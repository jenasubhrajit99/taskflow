package com.taskflow.common.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends TaskFlowException {

    public AuthorizationException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
}
