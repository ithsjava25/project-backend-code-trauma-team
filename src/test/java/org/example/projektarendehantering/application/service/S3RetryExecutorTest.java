package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.AppException;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3RetryExecutorTest {

    private final S3RetryExecutor executor = new S3RetryExecutor(3, 1, 5);

    @Test
    void execute_shouldRetryAndSucceedForTransientFailures() {
        int[] attempts = {0};

        String result = executor.execute("upload", context -> {
            attempts[0]++;
            if (attempts[0] < 3) {
                throw S3Exception.builder().statusCode(503).message("ServiceUnavailable").build();
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(attempts[0]).isEqualTo(3);
    }

    @Test
    void execute_shouldReturnDegradedCodeWhenTransientFailurePersists() {
        assertThatThrownBy(() -> executor.execute("download", context -> {
            throw SdkClientException.builder().message("Connection reset").build();
        }))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).errorCode()).isEqualTo("S3_SERVICE_DEGRADED"));
    }

    @Test
    void execute_shouldNotRetryPermanentFailures() {
        int[] attempts = {0};

        assertThatThrownBy(() -> executor.execute("delete", context -> {
            attempts[0]++;
            throw S3Exception.builder().statusCode(403).message("AccessDenied").build();
        }))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).errorCode()).isEqualTo("S3_DELETE_FAILED"));

        assertThat(attempts[0]).isEqualTo(1);
    }
}
