package com.blooming.blockchain.springbackend.exception;

import com.blooming.blockchain.springbackend.auth.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =============== Authentication Exceptions ===============
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.error("Authentication error: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .error("AUTHENTICATION_ERROR")
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex, WebRequest request) {
        log.error("JWT validation error: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message("Invalid or expired token")
            .error("JWT_ERROR")
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // =============== Business Logic Exceptions ===============
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        log.error("User not found: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .error("USER_NOT_FOUND")
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPointsException(InsufficientPointsException ex, WebRequest request) {
        log.warn("Insufficient points: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .error("INSUFFICIENT_POINTS")
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException ex, WebRequest request) {
        log.warn("Invalid request: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .error("INVALID_REQUEST")
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // =============== Validation Exceptions ===============
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((msg1, msg2) -> msg1 + ", " + msg2)
            .orElse("Validation failed");
            
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message(errorMessage)
            .error("VALIDATION_ERROR")
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        log.warn("Binding error: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message("Invalid request parameters")
            .error("BINDING_ERROR")
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Type mismatch error: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message("Invalid parameter type: " + ex.getName())
            .error("TYPE_MISMATCH_ERROR")
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // =============== Generic Exception Handler ===============
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message("An internal server error occurred")
            .error("INTERNAL_SERVER_ERROR")
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
