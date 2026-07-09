package com.taskflow.common.exception;

import com.taskflow.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleTaskFlowException_returnsCorrectStatusAndMessage() {
        TaskFlowException ex = new ResourceNotFoundException("Task", "id", "abc-123");

        ResponseEntity<ApiResponse<Void>> response = handler.handleTaskFlowException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Task", "id", "abc-123");
    }

    @Test
    void handleTaskFlowException_conflictException_returns409() {
        ConflictException ex = new ConflictException("Email already registered");

        ResponseEntity<ApiResponse<Void>> response = handler.handleTaskFlowException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("Email already registered");
    }

    @Test
    void handleTaskFlowException_authorizationException_returns403() {
        AuthorizationException ex = new AuthorizationException("Access denied");

        ResponseEntity<ApiResponse<Void>> response = handler.handleTaskFlowException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleTaskFlowException_authenticationException_returns401() {
        AuthenticationException ex = new AuthenticationException("Invalid credentials");

        ResponseEntity<ApiResponse<Void>> response = handler.handleTaskFlowException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleMethodArgumentNotValid_returnsFieldErrorMap() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "email", "must not be blank"));
        bindingResult.addError(new FieldError("target", "name", "size must be between 1 and 50"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response =
                handler.handleMethodArgumentNotValid(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getData())
                .containsEntry("email", "must not be blank")
                .containsEntry("name", "size must be between 1 and 50");
    }

    @Test
    void handleNoHandlerFound_returns404() {
        NoHandlerFoundException ex = new NoHandlerFoundException(
                "GET", "/api/v1/nonexistent", null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleNoHandlerFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("GET", "/api/v1/nonexistent");
    }

    @Test
    void handleGenericException_returns500WithSafeMessage() {
        Exception ex = new RuntimeException("Internal database error with sensitive details");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("database", "sensitive");
    }
}
