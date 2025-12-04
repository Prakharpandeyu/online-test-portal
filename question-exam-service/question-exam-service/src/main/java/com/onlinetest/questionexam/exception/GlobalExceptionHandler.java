package com.onlinetest.questionexam.exception;

import com.onlinetest.questionexam.dto.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.error("Validation error: {}", errors);

        ApiResponseDTO<Map<String, String>> response = ApiResponseDTO.error(
                "Validation failed");
        response.setData(errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseDTO<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage());

        ApiResponseDTO<Void> response = ApiResponseDTO.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        log.error("Illegal argument: {}", ex.getMessage());

        ApiResponseDTO<Void> response = ApiResponseDTO.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponseDTO<Void>> handleJwtException(
            io.jsonwebtoken.JwtException ex) {

        log.error("JWT error: {}", ex.getMessage());

        ApiResponseDTO<Void> response = ApiResponseDTO.error("Invalid or expired token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponseDTO<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                "An unexpected error occurred. Please try again later.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
