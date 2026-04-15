package org.example.projektarendehantering.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotAuthorizedException extends AppException {
    public NotAuthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }

    public NotAuthorizedException(String message) {
        super("NOT_AUTHORIZED", message);
    }
}
