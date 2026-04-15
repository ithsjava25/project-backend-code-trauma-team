package org.example.projektarendehantering.application.service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projektarendehantering.common.*;
import org.example.projektarendehantering.infrastructure.persistence.*;
import org.example.projektarendehantering.presentation.dto.DocumentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final S3Template s3Template;
    private final DocumentMapper documentMapper;
    private final AuditService auditService;

    @Value("${app.s3.bucket}")
    private String bucket;

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
                .filter(ce -> ce.getStatus() != CaseStatus.CLOSED)
                .orElseThrow(() -> new BadRequestException("Case not found"));

        validateAccess(actor, caseEntity);

        String s3Key = UUID.randomUUID().toString() + "-" + originalFilename;

        try {
            ObjectMetadata metadata = ObjectMetadata.builder()
                    .contentType(file.getContentType())
                    .build();
            s3Template.upload(bucket, s3Key, file.getInputStream(), metadata);
        } catch (Exception e) {
            log.error("S3 upload failed for bucket: {}, key: {}. Error: {}", bucket, s3Key, e.getMessage(), e);
            throw new AppException("S3_UPLOAD_FAILED", "Failed to upload file to S3");
        }

        boolean syncActive = TransactionSynchronizationManager.isSynchronizationActive();
        if (syncActive) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
                        try {
                            s3Template.deleteObject(bucket, s3Key);
                        } catch (Exception cleanupEx) {
                            log.error("Failed to cleanup rolled-back S3 object {}", s3Key, cleanupEx);
                        }
                    }
                }
            });
        }

        DocumentEntity entity = DocumentEntity.builder()
                .fileName(originalFilename)
                .s3Key(s3Key)
                .contentType(contentType)
                .fileSize(file.getSize())
                .uploadedAt(Instant.now())
                .uploadedBy(actor.userId())
                .caseEntity(caseEntity)
                .build();

        // --- SAFE DB SAVE WITH COMPENSATION IF IT FAILS ---
        try {
            DocumentEntity saved = documentRepository.save(entity);
            CaseStatus previousStatus = caseEntity.getStatus();
            if (previousStatus != CaseStatus.COMMUNICATION) {
                caseEntity.setStatus(CaseStatus.COMMUNICATION);
                caseRepository.save(caseEntity);

                String statusChange = (previousStatus != null ? previousStatus.name() : "NEW")
                        + " -> " + CaseStatus.COMMUNICATION.name();
                auditService.record(AuditEventEntity.builder()
                        .caseId(caseEntity.getId())
                        .statusChange(statusChange)
                        .actorId(actor.userId())
                        .actorRole(actor.role() != null ? actor.role().name() : null)
                        .build());
            }
            return documentMapper.toDTO(saved);
        } catch (RuntimeException ex) {
            if (!syncActive) {
                try {
                    s3Template.deleteObject(bucket, s3Key);
                } catch (Exception cleanupEx) {
                    log.error("Failed to cleanup orphaned S3 object {}", s3Key, cleanupEx);
                }
            }
            throw ex;
        }
    }

    public List<DocumentDTO> listDocuments(Actor actor, UUID caseId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .filter(ce -> ce.getStatus() != CaseStatus.CLOSED)
                .orElseThrow(() -> new BadRequestException("Case not found"));

        validateAccess(actor, caseEntity);

        return documentRepository.findAllByCaseEntityId(caseId).stream()
                .map(documentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public S3Resource downloadDocument(Actor actor, UUID documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new BadRequestException("Document not found"));

        validateAccess(actor, entity.getCaseEntity());

        return s3Template.download(bucket, entity.getS3Key());
    }

    @Transactional
    public void deleteDocument(Actor actor, UUID documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new BadRequestException("Document not found"));

        validateAccess(actor, entity.getCaseEntity());

        // Save S3 key before deletion
        String s3Key = entity.getS3Key();

        // 1. Delete DB entity inside the transaction
        documentRepository.delete(entity);

        // 2. Delete S3 object *after* successful commit (if in a transaction)
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                s3Template.deleteObject(bucket, s3Key);
                            } catch (Exception e) {
                                // DB is already committed — log but do not throw
                                log.error("Failed to delete S3 object {} after DB commit", s3Key, e);
                            }
                        }
                    }
            );
        } else {
            // No active transaction (e.g. in unit test)
            try {
                s3Template.deleteObject(bucket, s3Key);
            } catch (Exception e) {
                log.error("Failed to delete S3 object {} (no active transaction)", s3Key, e);
            }
        }
    }


    public DocumentEntity getEntity(Actor actor, UUID documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new BadRequestException("Document not found"));
        validateAccess(actor, entity.getCaseEntity());
        return entity;
    }

    private void validateAccess(Actor actor, CaseEntity caseEntity) {
        if (caseEntity.getStatus() == CaseStatus.CLOSED) {
            throw new NotAuthorizedException("Case is closed");
        }
        if (actor.isManager()) return;
        if (actor.isDoctor() && actor.userId().equals(caseEntity.getOwnerId())) return;
        if (actor.isNurse() && actor.userId().equals(caseEntity.getHandlerId())) return;
        throw new NotAuthorizedException("Not authorized to access documents for this case");
    }
}
