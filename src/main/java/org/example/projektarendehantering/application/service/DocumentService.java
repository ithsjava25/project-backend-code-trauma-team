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
import org.springframework.web.multipart.MultipartFile;

import org.apache.tika.Tika;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final S3Template s3Template;
    private final S3RetryExecutor s3RetryExecutor;
    private final FailedS3DeletionService failedS3DeletionService;
    private final DocumentMapper documentMapper;
    private final AuditService auditService;

    @Value("${app.s3.bucket}")
    private String bucket;

    private static final Tika TIKA = new Tika();
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final Map<String, String> EXTENSION_TO_MIME = Map.of(
            "pdf",  "application/pdf",
            "png",  "image/png",
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

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

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds the maximum allowed size of 10 MB");
        }

        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        String expectedMime = EXTENSION_TO_MIME.get(extension);

        if (expectedMime == null || !expectedMime.equalsIgnoreCase(contentType)) {
            throw new BadRequestException("File type not allowed. Allowed types: pdf, png, jpg, jpeg, docx");
        }

        byte[] fileBytes = file.getBytes();
        String detectedMime = TIKA.detect(fileBytes, originalFilename);
        if (!expectedMime.equalsIgnoreCase(detectedMime)) {
            throw new BadRequestException("File content does not match declared type");
        }

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BadRequestException("Case not found"));

        if (caseEntity.getStatus() == CaseStatus.CLOSED) {
            throw new BadRequestException("Case is closed");
        }

        validateAccess(actor, caseEntity);

        String s3Key = UUID.randomUUID() + "-" + originalFilename;

        ObjectMetadata metadata = ObjectMetadata.builder()                                                                                                                                                                                                                                       
                          .contentType(contentType)
                          .build();                                                                                                                                                                                                                                                                        
                  s3RetryExecutor.execute("upload", context -> {
                     s3Template.upload(bucket, s3Key, new ByteArrayInputStream(fileBytes), metadata);
                      return null;
                  });


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
                        .eventName("STATUS_CHANGED")
                        .description("Status changed to COMMUNICATION after document upload")
                        .actorId(actor.userId())
                        .actorRole(actor.role() != null ? actor.role().name() : null)
                        .occurredAt(Instant.now())
                        .build());
            } else {
                auditService.record(AuditEventEntity.builder()
                        .caseId(caseEntity.getId())
                        .eventName("DOCUMENT_UPLOADED")
                        .description("Document uploaded: " + entity.getFileName())
                        .actorId(actor.userId())
                        .actorRole(actor.role() != null ? actor.role().name() : null)
                        .occurredAt(Instant.now())
                        .build());
            }
            return documentMapper.toDTO(saved);
        } catch (RuntimeException ex) {
            try {
                s3RetryExecutor.execute("delete", context -> {
                    s3Template.deleteObject(bucket, s3Key);
                    return null;
                });
            } catch (Exception cleanupEx) {
                log.error("Failed to cleanup orphaned S3 object {}", s3Key, cleanupEx);
                failedS3DeletionService.enqueue(bucket, s3Key, cleanupEx);
                recordS3Audit(actor, caseEntity.getId(), "DOCUMENT_UPLOAD_COMPENSATION_QUEUED",
                        "Queued failed upload compensation cleanup for retry", s3Key);
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> listDocuments(Actor actor, UUID caseId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BadRequestException("Case not found"));

        if (caseEntity.getStatus() == CaseStatus.CLOSED) {
            throw new BadRequestException("Case is closed");
        }

        validateAccess(actor, caseEntity);

        return documentRepository.findAllByCaseEntityId(caseId).stream()
                .map(documentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public S3Resource downloadDocument(Actor actor, UUID documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new BadRequestException("Document not found"));

        validateAccess(actor, entity.getCaseEntity());

        return s3RetryExecutor.execute("download", context -> s3Template.download(bucket, entity.getS3Key()));
    }

    @Transactional
    public void deleteDocument(Actor actor, UUID documentId) {
        if (actor.isPatient()) {
            throw new NotAuthorizedException("Patients are not allowed to delete documents");
        }
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new BadRequestException("Document not found"));

        validateAccess(actor, entity.getCaseEntity());

        String s3Key = entity.getS3Key();
        documentRepository.delete(entity);
        try {
            s3RetryExecutor.execute("delete", context -> {
                s3Template.deleteObject(bucket, s3Key);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to delete S3 object {} after DB delete", s3Key, e);
            failedS3DeletionService.enqueue(bucket, s3Key, e);
        }
    }

    private void recordS3Audit(Actor actor, UUID caseId, String eventName, String description, String s3Key) {
        auditService.record(AuditEventEntity.builder()
                .caseId(caseId)
                .eventName(eventName)
                .description(description)
                .actorId(actor != null ? actor.userId() : null)
                .actorRole(actor != null && actor.role() != null ? actor.role().name() : null)
                .queryString("bucket=" + bucket + "&s3Key=" + s3Key)
                .occurredAt(Instant.now())
                .build());
    }


    @Transactional(readOnly = true)
    public DocumentEntity getEntity(Actor actor, UUID documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new BadRequestException("Document not found"));
        validateAccess(actor, entity.getCaseEntity());
        return entity;
    }

    private void validateAccess(Actor actor, CaseEntity caseEntity) {
        if (caseEntity.getStatus() == CaseStatus.CLOSED) {
            throw new BadRequestException("Case is closed");
        }
        if (actor.isManager()) return;
        if (actor.isDoctor() && actor.userId().equals(caseEntity.getOwnerId())) return;
        if (actor.isNurse() && actor.userId().equals(caseEntity.getHandlerId())) return;
        if (actor.isPatient() && caseEntity.getPatient() != null
                && actor.userId().equals(caseEntity.getPatient().getId())) return;
        throw new NotAuthorizedException("Not authorized to access documents for this case");
    }
}
