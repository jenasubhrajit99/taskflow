package com.taskflow.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends TaskFlowException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}
