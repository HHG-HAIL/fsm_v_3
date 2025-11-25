package com.fsm.task.infrastructure.config;

import com.fsm.task.application.exception.TechnicianNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler - specifically for TechnicianNotFoundException handling
 */
class GlobalExceptionHandlerTest {
    
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    
    @Test
    void testHandleTechnicianNotFoundException() {
        Long technicianId = 999L;
        TechnicianNotFoundException exception = new TechnicianNotFoundException(technicianId);
        
        ResponseEntity<Map<String, Object>> response = handler.handleTechnicianNotFoundException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Technician Not Found", body.get("error"));
        assertEquals(technicianId, body.get("technicianId"));
        assertTrue(body.get("message").toString().contains("not found"));
        assertNotNull(body.get("timestamp"));
    }
    
    @Test
    void testHandleTechnicianNotFoundExceptionWithReason() {
        Long technicianId = 102L;
        TechnicianNotFoundException exception = new TechnicianNotFoundException(technicianId, "is not active");
        
        ResponseEntity<Map<String, Object>> response = handler.handleTechnicianNotFoundException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(technicianId, body.get("technicianId"));
        assertTrue(body.get("message").toString().contains("not active"));
    }
}
