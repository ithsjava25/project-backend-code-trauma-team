package org.example.projektarendehantering.shared;

/**
 * Base application exception that carries a stable error code for clients/log aggregation.
 */
public class AppException extends RuntimeException {
    private final String errorCode;

    public AppException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
