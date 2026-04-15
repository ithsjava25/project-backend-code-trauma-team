package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.AuditEventEntity;
import org.example.projektarendehantering.presentation.dto.AuditEventDTO;
import org.springframework.stereotype.Component;

@Component
public class AuditEventMapper {

    public AuditEventDTO toDTO(AuditEventEntity entity) {
        if (entity == null) return null;
        return AuditEventDTO.builder()
                .id(entity.getId())
                .occurredAt(entity.getOccurredAt())
                .actorId(entity.getActorId())
                .actorRole(entity.getActorRole())
                .principalName(entity.getPrincipalName())
                .httpMethod(entity.getHttpMethod())
                .requestPath(entity.getRequestPath())
                .queryString(entity.getQueryString())
                .handler(entity.getHandler())
                .responseStatus(entity.getResponseStatus())
                .errorType(entity.getErrorType())
                .caseId(entity.getCaseId())
                .statusChange(entity.getStatusChange())
                .clientIp(entity.getClientIp())
                .userAgent(entity.getUserAgent())
                .build();
    }
}

