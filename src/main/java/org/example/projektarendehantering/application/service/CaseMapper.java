package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CaseMapper {

    private final CaseNoteMapper caseNoteMapper;
    private final DocumentMapper documentMapper;

    public CaseMapper(CaseNoteMapper caseNoteMapper, DocumentMapper documentMapper) {
        this.caseNoteMapper = caseNoteMapper;
        this.documentMapper = documentMapper;
    }

    public CaseDTO toDTO(CaseEntity entity) {
        if (entity == null) return null;
        CaseDTO dto = new CaseDTO(
                entity.getId(),
                entity.getStatus(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getPatient() != null ? entity.getPatient().getId() : null
        );
        if (entity.getNotes() != null) {
            dto.setNotes(entity.getNotes().stream()
                    .map(caseNoteMapper::toDTO)
                    .collect(Collectors.toList()));
        }
        if (entity.getDocuments() != null) {
            dto.setDocuments(entity.getDocuments().stream()
                    .map(documentMapper::toDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
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
