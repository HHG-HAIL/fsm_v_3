package com.fsm.task.infrastructure.config;

import com.fsm.task.application.exception.InvalidAssignmentException;
import com.fsm.task.application.exception.TaskNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Task Service.
 * Converts exceptions to appropriate HTTP responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handles IllegalArgumentException and returns 400 Bad Request.
     * 
     * @param ex the exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    /**
     * Handles constraint violation exceptions (e.g., from @Valid on request parameters)
     * 
     * @param ex the constraint violation exception
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        log.debug("Constraint violation: {}", ex.getMessage());
        
        String errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation failed");
        body.put("message", errors);
        
        return ResponseEntity.badRequest().body(body);
    }
    
    /**
     * Handles missing request parameter exceptions
     * 
     * @param ex the missing parameter exception
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.debug("Missing parameter: {}", ex.getMessage());
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Missing parameter");
        body.put("message", "Required parameter '" + ex.getParameterName() + "' is missing");
        
        return ResponseEntity.badRequest().body(body);
    }
    
    /**
     * Handles TaskNotFoundException and returns 404 Not Found.
     * 
     * @param ex the exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTaskNotFoundException(TaskNotFoundException ex) {
        log.warn("TaskNotFoundException: {}", ex.getMessage());
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    
    /**
     * Handles InvalidAssignmentException and returns 400 Bad Request.
     * 
     * @param ex the exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(InvalidAssignmentException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAssignmentException(InvalidAssignmentException ex) {
        log.warn("InvalidAssignmentException: {}", ex.getMessage());
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Invalid Assignment");
        body.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
