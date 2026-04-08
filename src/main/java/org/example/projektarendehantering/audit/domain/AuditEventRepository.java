package org.example.projektarendehantering.audit.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    Page<AuditEventEntity> findAllByOccurredAtBetweenOrderByOccurredAtDesc(Instant from, Instant to, Pageable pageable);

    Page<AuditEventEntity> findAllByCaseIdInAndOccurredAtBetweenOrderByOccurredAtDesc(
            Collection<UUID> caseIds,
            Instant from,
            Instant to,
            Pageable pageable
    );

    Page<AuditEventEntity> findAllByCaseIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            UUID caseId,
            Instant from,
            Instant to,
            Pageable pageable
    );
}
