package org.example.projektarendehantering.application.service;

import io.awspring.cloud.s3.S3Template;
import org.example.projektarendehantering.common.AppException;
import org.example.projektarendehantering.infrastructure.persistence.FailedS3DeletionEntity;
import org.example.projektarendehantering.infrastructure.persistence.FailedS3DeletionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FailedS3DeletionServiceTest {

    @Mock
    private FailedS3DeletionRepository failedS3DeletionRepository;
    @Mock
    private S3Template s3Template;
    @Mock
    private S3RetryExecutor s3RetryExecutor;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private FailedS3DeletionService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "batchSize", 10);
        ReflectionTestUtils.setField(service, "retryDelaySeconds", 1L);
    }

    @Test
    void enqueue_shouldPersistFailedDeletion() {
        when(failedS3DeletionRepository.existsByBucketAndS3Key(anyString(), anyString())).thenReturn(false);
        service.enqueue("bucket-a", "key-a", new RuntimeException("boom"));

        ArgumentCaptor<FailedS3DeletionEntity> captor = ArgumentCaptor.forClass(FailedS3DeletionEntity.class);
        verify(failedS3DeletionRepository).save(captor.capture());
        FailedS3DeletionEntity saved = captor.getValue();
        assertThat(saved.getBucket()).isEqualTo("bucket-a");
        assertThat(saved.getS3Key()).isEqualTo("key-a");
        assertThat(saved.getAttemptCount()).isZero();
        assertThat(saved.getLastError()).contains("boom");
        assertThat(saved.getNextAttemptAt()).isAfterOrEqualTo(Instant.now().minusSeconds(1));
    }

    @Test
    void processPendingDeletions_shouldDeleteEntryWhenRetrySucceeds() {
        FailedS3DeletionEntity pending = FailedS3DeletionEntity.builder()
                .bucket("bucket-a")
                .s3Key("key-a")
                .attemptCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .nextAttemptAt(Instant.now().minusSeconds(1))
                .build();

        when(failedS3DeletionRepository.findByNextAttemptAtBeforeOrderByCreatedAtAsc(any(), any(Pageable.class)))
                .thenReturn(List.of(pending));
        when(s3RetryExecutor.execute(eq("delete"), any())).thenReturn(null);

        service.processPendingDeletions();

        verify(failedS3DeletionRepository).delete(pending);
    }

    @Test
    void processPendingDeletions_shouldRescheduleWhenRetryFails() {
        FailedS3DeletionEntity pending = FailedS3DeletionEntity.builder()
                .bucket("bucket-a")
                .s3Key("key-a")
                .attemptCount(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .nextAttemptAt(Instant.now().minusSeconds(1))
                .build();

        when(failedS3DeletionRepository.findByNextAttemptAtBeforeOrderByCreatedAtAsc(any(), any(Pageable.class)))
                .thenReturn(List.of(pending));
        when(s3RetryExecutor.execute(eq("delete"), any()))
                .thenThrow(new AppException("S3_SERVICE_DEGRADED", "temporary issue"));

        service.processPendingDeletions();

        verify(failedS3DeletionRepository, atLeastOnce()).save(pending);
        assertThat(pending.getAttemptCount()).isEqualTo(2);
        assertThat(pending.getLastError()).contains("temporary issue");
    }
}
