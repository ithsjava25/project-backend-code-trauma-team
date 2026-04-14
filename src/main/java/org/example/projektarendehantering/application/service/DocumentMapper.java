package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.DocumentEntity;
import org.example.projektarendehantering.presentation.dto.DocumentDTO;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentDTO toDTO(DocumentEntity entity) {
        return new DocumentDTO(
            entity.getId(),
            entity.getFileName(),
            entity.getContentType(),
            entity.getFileSize(),
            entity.getUploadedAt(),
            entity.getUploadedBy(),
            entity.getCaseEntity().getId()
        );
    }
}
