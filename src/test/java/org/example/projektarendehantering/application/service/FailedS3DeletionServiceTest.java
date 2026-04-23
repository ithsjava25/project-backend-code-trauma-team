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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.RetryCallback;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @Mock
    private ObjectProvider<FailedS3DeletionService> selfProvider;

    @InjectMocks
    private FailedS3DeletionService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "batchSize", 10);
        ReflectionTestUtils.setField(service, "retryDelaySeconds", 1L);
        ReflectionTestUtils.setField(service, "maxAttempts", 3);
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
                .id(UUID.randomUUID())
                .bucket("bucket-a")
                .s3Key("key-a")
                .attemptCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .nextAttemptAt(Instant.now().minusSeconds(1))
                .build();

        when(failedS3DeletionRepository.findByNextAttemptAtBeforeOrderByCreatedAtAsc(any(), any(Pageable.class)))
                .thenReturn(List.of(pending));
        when(selfProvider.getObject()).thenReturn(service);
        when(failedS3DeletionRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(s3RetryExecutor.execute(eq("delete"), any())).thenAnswer(invocation -> {
            RetryCallback<Object, RuntimeException> callback = invocation.getArgument(1);
            return callback.doWithRetry(null);
        });

        service.processPendingDeletions();

        verify(s3Template).deleteObject("bucket-a", "key-a");
        verify(failedS3DeletionRepository).delete(pending);
    }

    @Test
    void processPendingDeletions_shouldRescheduleWhenRetryFails() {
        FailedS3DeletionEntity pending = FailedS3DeletionEntity.builder()
                .id(UUID.randomUUID())
                .bucket("bucket-a")
                .s3Key("key-a")
                .attemptCount(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .nextAttemptAt(Instant.now().minusSeconds(1))
                .build();

        when(failedS3DeletionRepository.findByNextAttemptAtBeforeOrderByCreatedAtAsc(any(), any(Pageable.class)))
                .thenReturn(List.of(pending));
        when(selfProvider.getObject()).thenReturn(service);
        when(failedS3DeletionRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(s3RetryExecutor.execute(eq("delete"), any()))
                .thenThrow(new AppException("S3_SERVICE_DEGRADED", "temporary issue"));

        service.processPendingDeletions();

        verify(failedS3DeletionRepository, atLeastOnce()).save(pending);
        assertThat(pending.getAttemptCount()).isEqualTo(2);
        assertThat(pending.getLastError()).contains("temporary issue");
    }

    @Test
    void processPendingDeletions_shouldDeadLetterWhenMaxAttemptsReached() {
        FailedS3DeletionEntity pending = FailedS3DeletionEntity.builder()
                .id(UUID.randomUUID())
                .bucket("bucket-a")
                .s3Key("key-a")
                .attemptCount(2)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .nextAttemptAt(Instant.now().minusSeconds(1))
                .build();

        when(failedS3DeletionRepository.findByNextAttemptAtBeforeOrderByCreatedAtAsc(any(), any(Pageable.class)))
                .thenReturn(List.of(pending));
        when(selfProvider.getObject()).thenReturn(service);
        when(failedS3DeletionRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(s3RetryExecutor.execute(eq("delete"), any()))
                .thenThrow(new AppException("S3_SERVICE_DEGRADED", "temporary issue"));

        service.processPendingDeletions();

        verify(failedS3DeletionRepository).delete(pending);
        verify(failedS3DeletionRepository, never()).save(pending);
    }
}
