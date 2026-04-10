package org.example.projektarendehantering.application.service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.AppException;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.infrastructure.persistence.DocumentEntity;
import org.example.projektarendehantering.infrastructure.persistence.DocumentRepository;
import org.example.projektarendehantering.presentation.dto.DocumentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final S3Template s3Template;
    private final DocumentMapper documentMapper;

    @Value("${app.s3.bucket}")
    private String bucket;

    public DocumentService(DocumentRepository documentRepository, CaseRepository caseRepository, S3Template s3Template, DocumentMapper documentMapper) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.s3Template = s3Template;
        this.documentMapper = documentMapper;
    }

    @Transactional
    public DocumentDTO uploadDocument(Actor actor, UUID caseId, MultipartFile file) throws IOException {
                if (file == null || file.isEmpty()) {
                        throw new BadRequestException("File is required");
                    }
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.isBlank()) {
                        throw new BadRequestException("File name is required");
                    }
                String contentType = file.getContentType();
                if (contentType == null || contentType.isBlank()) {
                       throw new BadRequestException("Content type is required");
                   }

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BadRequestException("Case not found"));

        // Basic authorization check (similar to other services)
        if (!actor.isManager() && !actor.userId().equals(caseEntity.getOwnerId()) && !actor.userId().equals(caseEntity.getHandlerId())) {
            throw new NotAuthorizedException("Not authorized to upload documents to this case");
        }

        String s3Key = UUID.randomUUID().toString() + "-" + originalFilename;

        try {
            ObjectMetadata metadata = ObjectMetadata.builder()
                    .contentType(file.getContentType())
                    .build();
            s3Template.upload(bucket, s3Key, file.getInputStream(), metadata);
        } catch (Exception e) {
            throw new AppException("S3_UPLOAD_FAILED", "Failed to upload file to S3: " + e.getMessage());
        }

        DocumentEntity entity = new DocumentEntity();
        entity.setFileName(originalFilename);
        entity.setS3Key(s3Key);
        entity.setContentType(contentType);
        entity.setFileSize(file.getSize());
        entity.setUploadedAt(Instant.now());
        entity.setUploadedBy(actor.userId());
        entity.setCaseEntity(caseEntity);

        DocumentEntity saved = documentRepository.save(entity);
        return documentMapper.toDTO(saved);
    }

    public List<DocumentDTO> listDocuments(Actor actor, UUID caseId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BadRequestException("Case not found"));

        // Basic authorization check
        if (!actor.isManager() && !actor.userId().equals(caseEntity.getOwnerId()) && !actor.userId().equals(caseEntity.getHandlerId())) {
            throw new NotAuthorizedException("Not authorized to view documents for this case");
        }

        return documentRepository.findAllByCaseEntityId(caseId).stream()
                .map(documentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public S3Resource downloadDocument(Actor actor, UUID documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new BadRequestException("Document not found"));

        CaseEntity caseEntity = entity.getCaseEntity();

        // Basic authorization check
        if (!actor.isManager() && !actor.userId().equals(caseEntity.getOwnerId()) && !actor.userId().equals(caseEntity.getHandlerId())) {
            throw new NotAuthorizedException("Not authorized to download this document");
        }

        return s3Template.download(bucket, entity.getS3Key());
    }

    @Transactional
    public void deleteDocument(Actor actor, UUID documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new BadRequestException("Document not found"));

        CaseEntity caseEntity = entity.getCaseEntity();

        // Basic authorization check
        if (!actor.isManager() && !actor.userId().equals(caseEntity.getOwnerId()) && !actor.userId().equals(caseEntity.getHandlerId())) {
            throw new NotAuthorizedException("Not authorized to delete this document");
        }

        try {
            s3Template.deleteObject(bucket, entity.getS3Key());
        } catch (Exception e) {
            // Log error but proceed with DB deletion if S3 delete fails? Or throw?
            // For now, let's throw to ensure consistency.
            throw new AppException("S3_DELETE_FAILED", "Failed to delete file from S3: " + e.getMessage());
        }

        documentRepository.delete(entity);
    }

    public DocumentEntity getEntity(UUID documentId) {
        return documentRepository.findById(documentId).orElseThrow(() -> new BadRequestException("Document not found"));
    }
}
