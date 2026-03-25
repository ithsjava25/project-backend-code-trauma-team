package org.example.projektarendehantering.domain;

import org.example.projektarendehantering.common.Actor;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain audit event value object.
 */
public record AuditEvent(
        AuditEventType type,
        Instant timestamp,
        UUID actorId,
        Map<String, String> targetIdentifiers,
        Map<String, String> details
) {

    public AuditEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(actorId, "actorId");
        targetIdentifiers = Map.copyOf(targetIdentifiers);
        details = Map.copyOf(details);
    }

    public static AuditEvent caseCreated(Actor actor, CaseId caseId, String title, String description) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(caseId, "caseId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(description, "description");

        Map<String, String> targetIdentifiers = Map.of(
                "caseId", caseId.value().toString()
        );
        Map<String, String> details = Map.of(
                "title", title,
                "description", description
        );

        return new AuditEvent(
                AuditEventType.CASE_CREATED,
                Instant.now(),
                actor.userId(),
                targetIdentifiers,
                details
        );
    }
}

