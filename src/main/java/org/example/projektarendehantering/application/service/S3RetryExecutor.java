package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Set;

@Component
public class S3RetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(S3RetryExecutor.class);
    private static final Set<Integer> RETRYABLE_HTTP_STATUS = Set.of(408, 429, 500, 502, 503, 504);

    private final RetryTemplate retryTemplate;

    public S3RetryExecutor(
            @Value("${app.s3.retry.max-attempts:3}") int maxAttempts,
            @Value("${app.s3.retry.initial-backoff-ms:200}") long initialBackoffMs,
            @Value("${app.s3.retry.max-backoff-ms:2000}") long maxBackoffMs
    ) {
        this.retryTemplate = createRetryTemplate(maxAttempts, initialBackoffMs, maxBackoffMs);
    }

    public <T> T execute(String operationName, RetryCallback<T, RuntimeException> callback) {
        try {
            return retryTemplate.execute(callback);
        } catch (RuntimeException ex) {
            throw mapToAppException(operationName, ex);
        }
    }

    public boolean isRetryable(Throwable throwable) {
        Throwable root = rootCause(throwable);

        if (root instanceof S3Exception s3Exception) {
            return RETRYABLE_HTTP_STATUS.contains(s3Exception.statusCode()) || isRetryableAwsErrorCode(s3Exception.awsErrorDetails() != null
                    ? s3Exception.awsErrorDetails().errorCode()
                    : null);
        }
        if (root instanceof AwsServiceException awsServiceException) {
            return RETRYABLE_HTTP_STATUS.contains(awsServiceException.statusCode());
        }
        return root instanceof SdkClientException;
    }

    private AppException mapToAppException(String operationName, RuntimeException ex) {
        String code;
        String message;
        if (isRetryable(ex)) {
            code = "S3_SERVICE_DEGRADED";
            message = "Temporary S3 issue while trying to " + operationName;
        } else {
            code = "S3_" + operationName.toUpperCase() + "_FAILED";
            message = "Failed to " + operationName + " file in S3";
        }
        log.error("S3 {} failed. code={}, retryable={}, error={}", operationName, code, isRetryable(ex), ex.getMessage(), ex);
        return new AppException(code, message, ex);
    }

    private RetryTemplate createRetryTemplate(int maxAttempts, long initialBackoffMs, long maxBackoffMs) {
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new SimpleRetryPolicy(maxAttempts) {
            @Override
            public boolean canRetry(RetryContext context) {
                if (!super.canRetry(context)) {
                    return false;
                }
                Throwable lastThrowable = context.getLastThrowable();
                return lastThrowable == null || isRetryable(lastThrowable);
            }
        });
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialBackoffMs);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(maxBackoffMs);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }

    private boolean isRetryableAwsErrorCode(String errorCode) {
        if (errorCode == null) {
            return false;
        }
        return switch (errorCode) {
            case "SlowDown", "RequestTimeout", "InternalError", "ServiceUnavailable" -> true;
            default -> false;
        };
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
