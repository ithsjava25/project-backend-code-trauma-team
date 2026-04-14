package org.example.projektarendehantering.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Configuration
public class S3Config {

    @Value("${app.s3.bucket}")
    private String bucketName;

    @Bean
    public ApplicationRunner initializeBucket(S3Client s3Client) {
        return args -> {
            try {
                s3Client.headBucket(HeadBucketRequest.builder()
                        .bucket(bucketName)
                        .build());
                log.info("S3 bucket '{}' already exists.", bucketName);
            } catch (S3Exception e) {
                if (e.statusCode() == 404) {
                    log.info("S3 bucket '{}' does not exist. Creating it...", bucketName);
                    s3Client.createBucket(CreateBucketRequest.builder()
                            .bucket(bucketName)
                            .build());
                    log.info("S3 bucket '{}' created successfully.", bucketName);
                } else {
                    log.error("Failed to check if S3 bucket '{}' exists", bucketName, e);
                    throw new IllegalStateException("S3 bucket initialization failed", e);
                }
            } catch (Exception e) {
                log.error("Unexpected error initializing S3 bucket '{}'", bucketName, e);
                throw new IllegalStateException("S3 bucket initialization failed", e);
            }
        };
    }
}
