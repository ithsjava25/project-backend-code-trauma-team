package org.example.projektarendehantering.common;

public class NotAuthorizedException extends AppException {
    public NotAuthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }

    public NotAuthorizedException(String message) {
        super("NOT_AUTHORIZED", message);
    }
}
