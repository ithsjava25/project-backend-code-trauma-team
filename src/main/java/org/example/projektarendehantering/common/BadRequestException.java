package org.example.projektarendehantering.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends AppException {
    public BadRequestException(String errorCode, String message) {
        super(errorCode, message);
    }

    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }
}
