package org.example.projektarendehantering.common;

public class BadRequestException extends AppException {
    public BadRequestException(String errorCode, String message) {
        super(errorCode, message);
    }

    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }
}
