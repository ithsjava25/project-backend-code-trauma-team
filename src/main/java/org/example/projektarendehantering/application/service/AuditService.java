package org.example.projektarendehantering.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.AuditEventEntity;
import org.example.projektarendehantering.infrastructure.persistence.AuditEventRepository;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.presentation.dto.AuditEventDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final AuditEventMapper auditEventMapper;
    private final CaseRepository caseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private static final String REDACTED = "[REDACTED]";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "pass",
            "pwd",
            "token",
            "access_token",
            "authorization",
            "apikey",
            "api_key",
            "secret",
            "ssn",
            "creditcard",
            "credit_card",
            "cardnumber",
            "card_number",
            "refresh_token"
    );

    @Transactional
    public void record(AuditEventEntity event) {
        if (event == null) return;
        if (event.getId() == null) {
            event.setId(UUID.randomUUID());
        }
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(Instant.now());
        }
        event.setQueryString(sanitizeAuditPayload(event.getQueryString()));
        auditEventRepository.save(event);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                broadcast(event);
            }
        });
    }

    private void broadcast(AuditEventEntity event) {
        if (emitters.isEmpty()) return;

        AuditEventDTO dto = auditEventMapper.toDTO(event);
        List<SseEmitter> failedEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("audit-event")
                        .data(dto));
            } catch (IOException | IllegalStateException e) {
                failedEmitters.add(emitter);
            }
        }
        emitters.removeAll(failedEmitters);
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 minute timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE emitter completed");
            emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE emitter timed out");
            emitters.remove(emitter);
        });
        emitter.onError((ex) -> {
            log.debug("SSE emitter error: {}", ex.getMessage());
            emitters.remove(emitter);
        });

        // Send an initial event to confirm connection
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Audit stream connected"));
            log.debug("SSE emitter created and initial event sent");
        } catch (IOException e) {
            log.error("Failed to send initial SSE event", e);
            emitters.remove(emitter);
        }

        return emitter;
    }

    private String sanitizeAuditPayload(String payload) {
        if (payload == null || payload.isBlank()) return payload;

        String trimmed = payload.trim();
        if (looksLikeJson(trimmed)) {
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                JsonNode sanitized = sanitizeJsonNode(node);
                return objectMapper.writeValueAsString(sanitized);
            } catch (JsonProcessingException ignored) {
                // Fall back to query-string sanitization below.
            }
        }
        return sanitizeQueryString(payload);
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeAuditPayload(Object payload) {
        if (payload == null) return null;
        if (payload instanceof String s) return sanitizeAuditPayload(s);

        if (payload instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String key = e.getKey() == null ? null : String.valueOf(e.getKey());
                Object value = e.getValue();
                if (key != null && isSensitiveKey(key)) {
                    out.put(key, REDACTED);
                } else {
                    out.put(key, sanitizeAuditPayload(value));
                }
            }
            return out;
        }

        if (payload instanceof Collection<?> col) {
            List<Object> out = new ArrayList<>(col.size());
            for (Object v : col) out.add(sanitizeAuditPayload(v));
            return out;
        }

        return payload;
    }

    private JsonNode sanitizeJsonNode(JsonNode node) {
        if (node == null) return null;
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node.deepCopy();
            obj.fieldNames().forEachRemaining(field -> {
                JsonNode value = obj.get(field);
                if (isSensitiveKey(field)) {
                    obj.put(field, REDACTED);
                } else {
                    obj.set(field, sanitizeJsonNode(value));
                }
            });
            return obj;
        }
        if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node.deepCopy();
            for (int i = 0; i < arr.size(); i++) {
                arr.set(i, sanitizeJsonNode(arr.get(i)));
            }
            return arr;
        }
        return node;
    }

    private String sanitizeQueryString(String query) {
        if (query == null || query.isBlank()) return query;

        String original = query;
        String prefix = "";
        String body = original;
        int qIdx = original.indexOf('?');
        if (qIdx >= 0) {
            prefix = original.substring(0, qIdx + 1);
            body = original.substring(qIdx + 1);
        }

        String[] parts = body.split("&", -1);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;

            int eq = part.indexOf('=');
            if (eq < 0) {
                String keyOnly = part;
                if (isSensitiveKey(keyOnly)) {
                    parts[i] = keyOnly + "=" + REDACTED;
                }
                continue;
            }

            String key = part.substring(0, eq);
            if (isSensitiveKey(key)) {
                parts[i] = key + "=" + REDACTED;
            }
        }
        return prefix + String.join("&", parts);
    }

    private boolean looksLikeJson(String s) {
        if (s == null) return false;
        String t = s.trim();
        return (t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"));
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) return false;
        String normalized = normalizeKey(key);
        if (SENSITIVE_KEYS.contains(normalized)) return true;

        // Catch common variants like "user.password", "authToken", "Authorization" etc.
        for (String sensitive : SENSITIVE_KEYS) {
            if (normalized.contains(sensitive)) return true;
        }
        return false;
    }

    private String normalizeKey(String key) {
        String k = key.trim();
        int dot = k.lastIndexOf('.');
        if (dot >= 0 && dot < k.length() - 1) {
            k = k.substring(dot + 1);
        }
        k = k.toLowerCase(Locale.ROOT);
        return k.replaceAll("[^a-z0-9_]", "");
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDTO> listEvents(Actor actor, Instant from, Instant to, UUID caseId, Pageable pageable) {
        requireActor(actor);
        Instant safeFrom = from != null ? from : Instant.EPOCH;
        Instant safeTo = to != null ? to : Instant.now();
        if (safeFrom.isAfter(safeTo)) {
            throw new IllegalArgumentException("Invalid time range: 'from' must be <= 'to'");
        }

        if (isManager(actor)) {
            if (caseId != null) {
                return auditEventRepository.findAllByCaseIdAndOccurredAtBetweenOrderByOccurredAtDesc(caseId, safeFrom, safeTo, pageable)
                        .map(auditEventMapper::toDTO);
            }
            return auditEventRepository.findAllByOccurredAtBetweenOrderByOccurredAtDesc(safeFrom, safeTo, pageable)
                    .map(auditEventMapper::toDTO);
        }

        if (isDoctor(actor) || isNurse(actor)) {
            Set<UUID> allowedCaseIds = allowedCaseIdsFor(actor);
            if (caseId != null) {
                if (!allowedCaseIds.contains(caseId)) {
                    throw new NotAuthorizedException("Not allowed to view audit events for this case");
                }
                return auditEventRepository.findAllByCaseIdAndOccurredAtBetweenOrderByOccurredAtDesc(caseId, safeFrom, safeTo, pageable)
                        .map(auditEventMapper::toDTO);
            }
            if (allowedCaseIds.isEmpty()) {
                return Page.empty(pageable);
            }
            return auditEventRepository.findAllByCaseIdInAndOccurredAtBetweenOrderByOccurredAtDesc(allowedCaseIds, safeFrom, safeTo, pageable)
                    .map(auditEventMapper::toDTO);
        }

        if (isPatient(actor)) {
            throw new NotAuthorizedException("Patients are not allowed to view audit events");
        }

        throw new NotAuthorizedException("Not allowed to view audit events");
    }

    private Set<UUID> allowedCaseIdsFor(Actor actor) {
        if (isDoctor(actor)) {
            return caseRepository.findAllByOwnerId(actor.userId()).stream()
                    .map(c -> c.getId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        if (isNurse(actor)) {
            return caseRepository.findAllByHandlerId(actor.userId()).stream()
                    .map(c -> c.getId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    private void requireActor(Actor actor) {
        if (actor == null || actor.userId() == null) {
            throw new NotAuthorizedException("Missing actor");
        }
    }

    private boolean isManager(Actor actor) {
        return actor.role() == Role.MANAGER;
    }

    private boolean isDoctor(Actor actor) {
        return actor.role() == Role.DOCTOR;
    }

    private boolean isNurse(Actor actor) {
        return actor.role() == Role.NURSE;
    }

    private boolean isPatient(Actor actor) {
        return actor.role() == Role.PATIENT;
    }
}

