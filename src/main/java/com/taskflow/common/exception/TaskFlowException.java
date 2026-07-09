package com.taskflow.common.exception;

import org.springframework.http.HttpStatus;

public class TaskFlowException extends RuntimeException {

    private final HttpStatus status;
    private final String     errorCode;

    public TaskFlowException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status    = status;
        this.errorCode = errorCode;
    }

    public TaskFlowException(String message, HttpStatus status) {
        this(message, status, status.name());
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
