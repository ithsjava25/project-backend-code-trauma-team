package org.example.projektarendehantering.application.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projektarendehantering.infrastructure.persistence.FailedS3DeletionEntity;
import org.example.projektarendehantering.infrastructure.persistence.FailedS3DeletionRepository;
import org.example.projektarendehantering.infrastructure.persistence.AuditEventEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailedS3DeletionService {

    private final FailedS3DeletionRepository failedS3DeletionRepository;
    private final S3Template s3Template;
    private final S3RetryExecutor s3RetryExecutor;
    private final AuditService auditService;

    @Value("${app.s3.failed-delete.batch-size:20}")
    private int batchSize;

    @Value("${app.s3.failed-delete.retry-delay-seconds:60}")
    private long retryDelaySeconds;

    @Transactional
    public void enqueue(String bucket, String s3Key, Exception e) {
        if (failedS3DeletionRepository.existsByBucketAndS3Key(bucket, s3Key)) {
            log.warn("Failed S3 deletion already queued. bucket={}, key={}", bucket, s3Key);
            return;
        }
        Instant now = Instant.now();
        FailedS3DeletionEntity entity = FailedS3DeletionEntity.builder()
                .bucket(bucket)
                .s3Key(s3Key)
                .attemptCount(0)
                .nextAttemptAt(now.plus(retryDelaySeconds, ChronoUnit.SECONDS))
                .createdAt(now)
                .updatedAt(now)
                .lastError(trimError(e))
                .build();
        failedS3DeletionRepository.save(entity);
        log.warn("Queued failed S3 deletion for retry. bucket={}, key={}", bucket, s3Key);
        auditService.record(AuditEventEntity.builder()
                .eventName("DOCUMENT_S3_DELETE_QUEUED")
                .description("Queued failed S3 deletion for retry")
                .queryString("bucket=" + bucket + "&s3Key=" + s3Key + "&error=" + trimError(e))
                .occurredAt(now)
                .build());
    }

    @Scheduled(fixedDelayString = "${app.s3.failed-delete.scheduler-delay-ms:30000}")
    @Transactional
    public void processPendingDeletions() {
        List<FailedS3DeletionEntity> items = failedS3DeletionRepository
                .findByNextAttemptAtBeforeOrderByCreatedAtAsc(Instant.now(), PageRequest.of(0, batchSize));

        for (FailedS3DeletionEntity item : items) {
            try {
                s3RetryExecutor.execute("delete", context -> {
                    s3Template.deleteObject(item.getBucket(), item.getS3Key());
                    return null;
                });
                failedS3DeletionRepository.delete(item);
                log.info("Recovered failed S3 deletion. bucket={}, key={}", item.getBucket(), item.getS3Key());
                auditService.record(AuditEventEntity.builder()
                        .eventName("DOCUMENT_S3_DELETE_RECOVERED")
                        .description("Recovered failed S3 deletion from retry queue")
                        .queryString("bucket=" + item.getBucket() + "&s3Key=" + item.getS3Key() + "&attempts=" + item.getAttemptCount())
                        .occurredAt(Instant.now())
                        .build());
            } catch (Exception ex) {
                item.setAttemptCount(item.getAttemptCount() + 1);
                item.setUpdatedAt(Instant.now());
                item.setNextAttemptAt(Instant.now().plus(retryDelaySeconds, ChronoUnit.SECONDS));
                item.setLastError(trimError(ex));
                failedS3DeletionRepository.save(item);
                log.warn("Failed retrying S3 deletion. bucket={}, key={}, attempts={}",
                        item.getBucket(), item.getS3Key(), item.getAttemptCount(), ex);
                auditService.record(AuditEventEntity.builder()
                        .eventName("DOCUMENT_S3_DELETE_RETRY_FAILED")
                        .description("Retry failed for queued S3 deletion")
                        .queryString("bucket=" + item.getBucket() + "&s3Key=" + item.getS3Key()
                                + "&attempts=" + item.getAttemptCount() + "&error=" + trimError(ex))
                        .occurredAt(Instant.now())
                        .build());
            }
        }
    }

    private String trimError(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return e.getClass().getSimpleName();
        }
        return message.length() > 900 ? message.substring(0, 900) : message;
    }
}
