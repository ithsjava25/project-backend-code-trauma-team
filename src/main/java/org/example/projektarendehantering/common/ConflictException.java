package org.example.projektarendehantering.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends AppException {
    public ConflictException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}
