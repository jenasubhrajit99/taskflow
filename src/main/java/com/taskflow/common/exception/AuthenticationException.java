package com.taskflow.common.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends TaskFlowException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }
}
