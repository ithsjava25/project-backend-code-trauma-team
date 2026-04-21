package org.example.projektarendehantering.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.CaseStatus;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseRealtimeService {

    private static final long EMITTER_TIMEOUT_MS = 15 * 60 * 1000L;

    private final CaseRepository caseRepository;

    private final Map<UUID, ListSubscription> listSubscribers = new ConcurrentHashMap<>();
    private final Map<UUID, CaseSubscription> caseSubscribers = new ConcurrentHashMap<>();

    public SseEmitter subscribeToCaseList(Actor actor) {
        if (actor == null || actor.userId() == null) {
            throw new NotAuthorizedException("Missing actor");
        }

        UUID subscriptionId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        listSubscribers.put(subscriptionId, new ListSubscription(actor, emitter));
        bindLifecycle(subscriptionId, emitter, true);
        sendEvent(emitter, "connected", new CaseRealtimeEvent("connected", null, Instant.now(), "list-stream"));
        return emitter;
    }

    public SseEmitter subscribeToCase(Actor actor, UUID caseId) {
        if (actor == null || actor.userId() == null) {
            throw new NotAuthorizedException("Missing actor");
        }
        if (caseId == null) {
            throw new IllegalArgumentException("caseId is required");
        }
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));
        if (!canRead(actor, caseEntity)) {
            throw new NotAuthorizedException("Not allowed to subscribe to this case");
        }

        UUID subscriptionId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        caseSubscribers.put(subscriptionId, new CaseSubscription(actor, caseId, emitter));
        bindLifecycle(subscriptionId, emitter, false);
        sendEvent(emitter, "connected", new CaseRealtimeEvent("connected", caseId, Instant.now(), "case-stream"));
        return emitter;
    }

    public void publishCaseEvent(UUID caseId, String eventType, String message) {
        CaseRealtimeEvent event = new CaseRealtimeEvent(eventType, caseId, Instant.now(), message);
        broadcastCaseEvent(event);
    }

    @Scheduled(fixedDelay = 25000L)
    public void keepAlive() {
        CaseRealtimeEvent heartbeat = new CaseRealtimeEvent("keepalive", null, Instant.now(), "heartbeat");
        listSubscribers.forEach((id, sub) -> {
            if (!sendEvent(sub.emitter(), "keepalive", heartbeat)) {
                listSubscribers.remove(id);
            }
        });
        caseSubscribers.forEach((id, sub) -> {
            CaseRealtimeEvent caseHeartbeat = new CaseRealtimeEvent("keepalive", sub.caseId(), Instant.now(), "heartbeat");
            if (!sendEvent(sub.emitter(), "keepalive", caseHeartbeat)) {
                caseSubscribers.remove(id);
            }
        });
    }

    private void broadcastCaseEvent(CaseRealtimeEvent event) {
        if (event.caseId() == null) {
            return;
        }

        CaseEntity caseEntity = caseRepository.findById(event.caseId()).orElse(null);
        if (caseEntity == null) {
            return;
        }

        listSubscribers.forEach((id, sub) -> {
            if (!canRead(sub.actor(), caseEntity)) {
                return;
            }
            if (!sendEvent(sub.emitter(), event.eventType(), event)) {
                listSubscribers.remove(id);
            }
        });

        caseSubscribers.forEach((id, sub) -> {
            if (!event.caseId().equals(sub.caseId())) {
                return;
            }
            if (!canRead(sub.actor(), caseEntity)) {
                caseSubscribers.remove(id);
                closeEmitter(sub.emitter());
                return;
            }
            if (!sendEvent(sub.emitter(), event.eventType(), event)) {
                caseSubscribers.remove(id);
            }
        });
    }

    private boolean canRead(Actor actor, CaseEntity entity) {
        if (actor == null || actor.role() == null) {
            return false;
        }
        if (actor.role() == Role.MANAGER) {
            return true;
        }
        if (entity.getStatus() == CaseStatus.CLOSED) {
            return false;
        }
        if (actor.role() == Role.DOCTOR && actor.userId().equals(entity.getOwnerId())) {
            return true;
        }
        if (actor.role() == Role.NURSE && actor.userId().equals(entity.getHandlerId())) {
            return true;
        }
        return false;
    }

    private void bindLifecycle(UUID subscriptionId, SseEmitter emitter, boolean listStream) {
        emitter.onCompletion(() -> remove(subscriptionId, listStream));
        emitter.onTimeout(() -> remove(subscriptionId, listStream));
        emitter.onError(ex -> remove(subscriptionId, listStream));
    }

    private void remove(UUID subscriptionId, boolean listStream) {
        if (listStream) {
            listSubscribers.remove(subscriptionId);
        } else {
            caseSubscribers.remove(subscriptionId);
        }
    }

    private boolean sendEvent(SseEmitter emitter, String eventName, CaseRealtimeEvent payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload));
            return true;
        } catch (IOException ex) {
            log.debug("Failed to send SSE event '{}': {}", eventName, ex.getMessage());
            closeEmitter(emitter);
            return false;
        }
    }

    private void closeEmitter(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (RuntimeException ignored) {
            // no-op
        }
    }

    private record ListSubscription(Actor actor, SseEmitter emitter) {
    }

    private record CaseSubscription(Actor actor, UUID caseId, SseEmitter emitter) {
    }
}
