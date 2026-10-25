package com.pet_project.watchstore.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidPasswordResetCodeException extends ResponseStatusException {

    public InvalidPasswordResetCodeException(HttpStatus status, String message) {
        super(status, message);
    }
}
