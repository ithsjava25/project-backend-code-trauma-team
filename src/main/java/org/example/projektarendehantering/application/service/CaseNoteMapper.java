package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.infrastructure.persistence.CaseNoteEntity;
import org.example.projektarendehantering.presentation.dto.CaseNoteDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CaseNoteMapper {

    public CaseNoteDTO toDTO(CaseNoteEntity entity) {
        if (entity == null) return null;
        return CaseNoteDTO.builder()
            .id(entity.getId())
            .content(entity.getContent())
            .authorDisplayName(entity.getAuthorDisplayName())
            .authorGithubUsername(entity.getAuthorGithubUsername())
            .authorRole(entity.getAuthorRole())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    public CaseNoteEntity toEntity(CaseNoteDTO dto) {
        if (dto == null) return null;
        return CaseNoteEntity.builder()
            .id(dto.getId())
            .content(dto.getContent())
            .authorDisplayName(dto.getAuthorDisplayName())
            .authorGithubUsername(dto.getAuthorGithubUsername())
            .authorRole(dto.getAuthorRole())
            .createdAt(dto.getCreatedAt())
            .build();
    }

    public CaseNoteEntity toEntity(Actor actor, String content) {
        if (actor == null) return null;
        return CaseNoteEntity.builder()
            .content(content)
            .authorDisplayName(actor.displayName())
            .authorGithubUsername(actor.githubUsername())
            .authorRole(actor.role() != null ? actor.role().name() : null)
            .createdAt(Instant.now())
            .build();
    }
}
