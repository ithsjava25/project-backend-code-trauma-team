package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "failed_s3_deletions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bucket", "s3_key"}),
        indexes = @Index(name = "idx_failed_s3_next_attempt", columnList = "next_attempt_at,created_at")
)
public class FailedS3DeletionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String bucket;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(length = 1000)
    private String lastError;
}
