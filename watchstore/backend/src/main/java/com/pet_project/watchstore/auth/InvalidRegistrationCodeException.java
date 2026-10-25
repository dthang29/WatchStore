package com.pet_project.watchstore.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidRegistrationCodeException extends ResponseStatusException {

    public InvalidRegistrationCodeException(HttpStatus status, String message) {
        super(status, message);
    }
}
