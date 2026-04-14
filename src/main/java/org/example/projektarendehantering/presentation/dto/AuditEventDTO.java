package org.example.projektarendehantering.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDTO {

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

    private String clientIp;
    private String userAgent;

}
