package com.taskflow.common.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends TaskFlowException {

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
}
