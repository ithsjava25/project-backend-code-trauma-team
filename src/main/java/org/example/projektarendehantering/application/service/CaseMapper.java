package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CaseMapper {

    private final CaseNoteMapper caseNoteMapper;

    public CaseMapper(CaseNoteMapper caseNoteMapper) {
        this.caseNoteMapper = caseNoteMapper;
    }

    public CaseDTO toDTO(CaseEntity entity) {
        if (entity == null) return null;
        CaseDTO dto = CaseDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .patientId(entity.getPatient() != null ? entity.getPatient().getId() : null)
                .build();

        if (entity.getNotes() != null) {
            dto.setNotes(entity.getNotes().stream()
                    .map(caseNoteMapper::toDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public CaseEntity toEntity(CaseDTO dto) {
        if (dto == null) return null;
        return CaseEntity.builder()
                .id(dto.getId())
                .status(dto.getStatus())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
