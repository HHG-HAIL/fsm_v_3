package com.fsm.task.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an assignment operation is invalid.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAssignmentException extends RuntimeException {
    
    public InvalidAssignmentException(String message) {
        super(message);
    }
}
