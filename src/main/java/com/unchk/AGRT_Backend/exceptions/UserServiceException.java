package com.unchk.AGRT_Backend.exceptions;

import org.springframework.http.HttpStatus;

public class UserServiceException extends RuntimeException {
    private final HttpStatus status;
    public UserServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() {
        return status;
    }
}