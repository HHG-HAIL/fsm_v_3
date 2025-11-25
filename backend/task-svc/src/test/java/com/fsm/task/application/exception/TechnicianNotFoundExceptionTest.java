package com.fsm.task.application.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TechnicianNotFoundException
 */
class TechnicianNotFoundExceptionTest {
    
    @Test
    void testExceptionWithIdOnly() {
        Long technicianId = 123L;
        
        TechnicianNotFoundException exception = new TechnicianNotFoundException(technicianId);
        
        assertEquals(technicianId, exception.getTechnicianId());
        assertEquals("Technician not found with ID: 123", exception.getMessage());
    }
    
    @Test
    void testExceptionWithIdAndReason() {
        Long technicianId = 456L;
        String reason = "is not active";
        
        TechnicianNotFoundException exception = new TechnicianNotFoundException(technicianId, reason);
        
        assertEquals(technicianId, exception.getTechnicianId());
        assertEquals("Technician with ID 456 is not active", exception.getMessage());
    }
    
    @Test
    void testExceptionIsRuntimeException() {
        TechnicianNotFoundException exception = new TechnicianNotFoundException(1L);
        
        assertTrue(exception instanceof RuntimeException);
    }
}
