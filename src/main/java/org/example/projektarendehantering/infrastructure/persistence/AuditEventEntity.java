package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "audit_events",
        indexes = {
                @Index(name = "idx_audit_events_occurred_at", columnList = "occurredAt"),
                @Index(name = "idx_audit_events_actor_id", columnList = "actorId"),
                @Index(name = "idx_audit_events_case_id", columnList = "caseId")
        }
)
public class AuditEventEntity {

    @Id
    private UUID id;

    private Instant occurredAt;

    private UUID actorId;
    private String actorRole;
    private String principalName;

    private String httpMethod;
    private String requestPath;
    private String queryString;
    private String handler;

    private Integer responseStatus;
    private String errorType;

    private UUID caseId;

    private String statusChange;

    private String clientIp;
    private String userAgent;
}
