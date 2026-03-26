package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.stereotype.Component;

@Component
public class CaseMapper {

    public CaseDTO toDTO(CaseEntity entity) {
        if (entity == null) return null;
        return new CaseDTO(
                entity.getId(),
                entity.getStatus(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }

    public CaseEntity toEntity(CaseDTO dto) {
        if (dto == null) return null;
        CaseEntity entity = new CaseEntity();
        entity.setId(dto.getId());
        entity.setStatus(dto.getStatus());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }
}
