package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.AuditEventEntity;
import org.example.projektarendehantering.presentation.dto.AuditEventDTO;
import org.springframework.stereotype.Component;

@Component
public class AuditEventMapper {

    public AuditEventDTO toDTO(AuditEventEntity entity) {
        if (entity == null) return null;
        AuditEventDTO dto = new AuditEventDTO();
        dto.setId(entity.getId());
        dto.setOccurredAt(entity.getOccurredAt());
        dto.setActorId(entity.getActorId());
        dto.setActorRole(entity.getActorRole());
        dto.setPrincipalName(entity.getPrincipalName());
        dto.setHttpMethod(entity.getHttpMethod());
        dto.setRequestPath(entity.getRequestPath());
        dto.setQueryString(entity.getQueryString());
        dto.setHandler(entity.getHandler());
        dto.setResponseStatus(entity.getResponseStatus());
        dto.setErrorType(entity.getErrorType());
        dto.setCaseId(entity.getCaseId());
        dto.setClientIp(entity.getClientIp());
        dto.setUserAgent(entity.getUserAgent());
        return dto;
    }
}

