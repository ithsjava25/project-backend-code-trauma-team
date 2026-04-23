package org.example.projektarendehantering.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FailedS3DeletionRepository extends JpaRepository<FailedS3DeletionEntity, UUID> {
    List<FailedS3DeletionEntity> findByNextAttemptAtBeforeOrderByCreatedAtAsc(Instant now, Pageable pageable);
    boolean existsByBucketAndS3Key(String bucket, String s3Key);
}
