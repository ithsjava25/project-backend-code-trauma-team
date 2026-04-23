package org.example.projektarendehantering.application.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projektarendehantering.infrastructure.persistence.FailedS3DeletionEntity;
import org.example.projektarendehantering.infrastructure.persistence.FailedS3DeletionRepository;
import org.example.projektarendehantering.infrastructure.persistence.AuditEventEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailedS3DeletionService {

    private final FailedS3DeletionRepository failedS3DeletionRepository;
    private final S3Template s3Template;
    private final S3RetryExecutor s3RetryExecutor;
    private final AuditService auditService;
    private final ObjectProvider<FailedS3DeletionService> selfProvider;

    @Value("${app.s3.failed-delete.batch-size:20}")
    private int batchSize;

    @Value("${app.s3.failed-delete.retry-delay-seconds:60}")
    private long retryDelaySeconds;

    @Value("${app.s3.failed-delete.max-attempts:10}")
    private int maxAttempts;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueue(String bucket, String s3Key, Exception e) {
        if (failedS3DeletionRepository.existsByBucketAndS3Key(bucket, s3Key)) {
            log.warn("Failed S3 deletion already queued. bucket={}, key={}", bucket, s3Key);
            return;
        }
        Instant now = Instant.now();
        String trimmed = trimError(e);
        FailedS3DeletionEntity entity = FailedS3DeletionEntity.builder()
                .bucket(bucket)
                .s3Key(s3Key)
                .attemptCount(0)
                .nextAttemptAt(now.plus(retryDelaySeconds, ChronoUnit.SECONDS))
                .createdAt(now)
                .updatedAt(now)
                .lastError(trimmed)
                .build();
        failedS3DeletionRepository.save(entity);
        log.warn("Queued failed S3 deletion for retry. bucket={}, key={}", bucket, s3Key);
        recordAuditBestEffort(AuditEventEntity.builder()
                .eventName("DOCUMENT_S3_DELETE_QUEUED")
                .description("Queued failed S3 deletion for retry")
                .queryString("bucket=" + bucket + "&s3Key=" + s3Key + "&error=" + trimmed)
                .occurredAt(now)
                .build());
    }

    @Scheduled(fixedDelayString = "${app.s3.failed-delete.scheduler-delay-ms:30000}")
    public void processPendingDeletions() {
        List<FailedS3DeletionEntity> items = failedS3DeletionRepository
                .findByNextAttemptAtBeforeOrderByCreatedAtAsc(Instant.now(), PageRequest.of(0, batchSize));

        for (FailedS3DeletionEntity item : items) {
            try {
                selfProvider.getObject().processOne(item.getId());
            } catch (Exception ex) {
                log.error("Unexpected error while processing failed S3 deletion item. id={}", item.getId(), ex);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(UUID id) {
        FailedS3DeletionEntity item = failedS3DeletionRepository.findById(id).orElse(null);
        if (item == null) {
            return;
        }
        if (item.getAttemptCount() >= maxAttempts) {
            markDeadLetter(item, "max attempts exceeded");
            return;
        }
        try {
            s3RetryExecutor.execute("delete", context -> {
                s3Template.deleteObject(item.getBucket(), item.getS3Key());
                return null;
            });
            failedS3DeletionRepository.delete(item);
            log.info("Recovered failed S3 deletion. bucket={}, key={}", item.getBucket(), item.getS3Key());
            recordAuditBestEffort(AuditEventEntity.builder()
                    .eventName("DOCUMENT_S3_DELETE_RECOVERED")
                    .description("Recovered failed S3 deletion from retry queue")
                    .queryString("bucket=" + item.getBucket() + "&s3Key=" + item.getS3Key() + "&attempts=" + item.getAttemptCount())
                    .occurredAt(Instant.now())
                    .build());
        } catch (Exception ex) {
            int nextAttemptCount = item.getAttemptCount() + 1;
            item.setAttemptCount(nextAttemptCount);
            item.setUpdatedAt(Instant.now());
            item.setLastError(trimError(ex));

            if (nextAttemptCount >= maxAttempts) {
                markDeadLetter(item, trimError(ex));
                log.warn("Giving up failed S3 deletion after max attempts. bucket={}, key={}, attempts={}",
                        item.getBucket(), item.getS3Key(), item.getAttemptCount(), ex);
                return;
            }

            long delaySeconds = computeRetryDelaySeconds(nextAttemptCount);
            item.setNextAttemptAt(Instant.now().plus(delaySeconds, ChronoUnit.SECONDS));
            failedS3DeletionRepository.save(item);
            log.warn("Failed retrying S3 deletion. bucket={}, key={}, attempts={}",
                    item.getBucket(), item.getS3Key(), item.getAttemptCount(), ex);
            recordAuditBestEffort(AuditEventEntity.builder()
                    .eventName("DOCUMENT_S3_DELETE_RETRY_FAILED")
                    .description("Retry failed for queued S3 deletion")
                    .queryString("bucket=" + item.getBucket() + "&s3Key=" + item.getS3Key()
                            + "&attempts=" + item.getAttemptCount() + "&error=" + trimError(ex))
                    .occurredAt(Instant.now())
                    .build());
        }
    }

    private long computeRetryDelaySeconds(int attemptCount) {
        long baseDelay = Math.max(1L, retryDelaySeconds);
        long multiplier = Math.min(Math.max(1, attemptCount), 10);
        return baseDelay * multiplier;
    }

    private void markDeadLetter(FailedS3DeletionEntity item, String reason) {
        failedS3DeletionRepository.delete(item);
        recordAuditBestEffort(AuditEventEntity.builder()
                .eventName("DOCUMENT_S3_DELETE_DEAD_LETTERED")
                .description("Queued S3 deletion reached max retry attempts and was dead-lettered")
                .queryString("bucket=" + item.getBucket() + "&s3Key=" + item.getS3Key()
                        + "&attempts=" + item.getAttemptCount() + "&reason=" + reason)
                .occurredAt(Instant.now())
                .build());
    }

    private void recordAuditBestEffort(AuditEventEntity event) {
        try {
            auditService.record(event);
        } catch (Exception ex) {
            log.warn("Failed to record S3 deletion audit event. eventName={}", event.getEventName(), ex);
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
