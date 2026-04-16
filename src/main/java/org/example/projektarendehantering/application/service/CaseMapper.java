package org.example.projektarendehantering.application.service;

import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CaseMapper {

    private final CaseNoteMapper caseNoteMapper;
    private final DocumentMapper documentMapper;

    public CaseDTO toDTO(CaseEntity entity) {
        if (entity == null) return null;
        CaseDTO dto = CaseDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .patientId(entity.getPatient() != null ? entity.getPatient().getId() : null)
                .ownerId(entity.getOwnerId())
                .handlerId(entity.getHandlerId())
                .build();

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
        return CaseEntity.builder()
                .id(dto.getId())
                .status(dto.getStatus())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .createdAt(dto.getCreatedAt())
                .ownerId(dto.getOwnerId())
                .handlerId(dto.getHandlerId())
                .build();
    }
}
