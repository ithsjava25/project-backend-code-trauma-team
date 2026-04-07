package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.CaseNoteEntity;
import org.example.projektarendehantering.presentation.dto.CaseNoteDTO;
import org.springframework.stereotype.Component;

@Component
public class CaseNoteMapper {

    public CaseNoteDTO toDTO(CaseNoteEntity entity) {
        if (entity == null) return null;
        return new CaseNoteDTO(
            entity.getId(),
            entity.getContent(),
            entity.getAuthorDisplayName(),
            entity.getAuthorGithubUsername(),
            entity.getAuthorRole(),
            entity.getCreatedAt()
        );
    }

    public CaseNoteEntity toEntity(CaseNoteDTO dto) {
        if (dto == null) return null;
        CaseNoteEntity entity = new CaseNoteEntity();
        entity.setId(dto.getId());
        entity.setContent(dto.getContent());
        entity.setAuthorDisplayName(dto.getAuthorDisplayName());
        entity.setAuthorGithubUsername(dto.getAuthorGithubUsername());
        entity.setAuthorRole(dto.getAuthorRole());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }
}
