package org.example.projektarendehantering.application.service;

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

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final AuditEventMapper auditEventMapper;
    private final CaseRepository caseRepository;

    public AuditService(AuditEventRepository auditEventRepository, AuditEventMapper auditEventMapper, CaseRepository caseRepository) {
        this.auditEventRepository = auditEventRepository;
        this.auditEventMapper = auditEventMapper;
        this.caseRepository = caseRepository;
    }

    @Transactional
    public void record(AuditEventEntity event) {
        if (event == null) return;
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(Instant.now());
        }
        auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDTO> listEvents(Actor actor, Instant from, Instant to, UUID caseId, Pageable pageable) {
        requireActor(actor);
        Instant safeFrom = from != null ? from : Instant.EPOCH;
        Instant safeTo = to != null ? to : Instant.now();

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
            if (allowedCaseIds.isEmpty()) {
                return Page.empty(pageable);
            }
            if (caseId != null) {
                if (!allowedCaseIds.contains(caseId)) {
                    throw new NotAuthorizedException("Not allowed to view audit events for this case");
                }
                return auditEventRepository.findAllByCaseIdAndOccurredAtBetweenOrderByOccurredAtDesc(caseId, safeFrom, safeTo, pageable)
                        .map(auditEventMapper::toDTO);
            }
            return auditEventRepository.findAllByCaseIdInAndOccurredAtBetweenOrderByOccurredAtDesc(allowedCaseIds, safeFrom, safeTo, pageable)
                    .map(auditEventMapper::toDTO);
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
        return actor.role() == Role.MANAGER || actor.role() == Role.ADMIN;
    }

    private boolean isDoctor(Actor actor) {
        return actor.role() == Role.DOCTOR || actor.role() == Role.CASE_OWNER;
    }

    private boolean isNurse(Actor actor) {
        return actor.role() == Role.NURSE || actor.role() == Role.HANDLER;
    }
}

