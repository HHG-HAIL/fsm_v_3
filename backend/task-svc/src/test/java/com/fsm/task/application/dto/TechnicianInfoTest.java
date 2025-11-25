package com.fsm.task.application.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TechnicianInfo DTO
 */
class TechnicianInfoTest {
    
    @Test
    void testIsActiveWithActiveStatus() {
        TechnicianInfo info = TechnicianInfo.builder()
                .id(1L)
                .name("Test Technician")
                .status("ACTIVE")
                .build();
        
        assertTrue(info.isActive());
    }
    
    @Test
    void testIsActiveWithInactiveStatus() {
        TechnicianInfo info = TechnicianInfo.builder()
                .id(1L)
                .name("Test Technician")
                .status("INACTIVE")
                .build();
        
        assertFalse(info.isActive());
    }
    
    @Test
    void testIsActiveWithNullStatus() {
        TechnicianInfo info = TechnicianInfo.builder()
                .id(1L)
                .name("Test Technician")
                .status(null)
                .build();
        
        assertFalse(info.isActive());
    }
    
    @Test
    void testIsActiveCaseInsensitive() {
        TechnicianInfo info = TechnicianInfo.builder()
                .id(1L)
                .status("active")
                .build();
        
        assertTrue(info.isActive());
    }
    
    @Test
    void testIsTechnicianWithTechnicianRole() {
        TechnicianInfo info = TechnicianInfo.builder()
                .id(1L)
                .role("TECHNICIAN")
                .build();
        
        assertTrue(info.isTechnician());
    }
    
    @Test
    void testIsTechnicianWithDispatcherRole() {
        TechnicianInfo info = TechnicianInfo.builder()
                .id(1L)
                .role("DISPATCHER")
                .build();
        
        assertFalse(info.isTechnician());
    }
    
    @Test
    void testIsTechnicianWithNullRole() {
        TechnicianInfo info = TechnicianInfo.builder()
                .id(1L)
                .role(null)
                .build();
        
        assertFalse(info.isTechnician());
    }
    
    @Test
    void testIsTechnicianCaseInsensitive() {
        TechnicianInfo info = TechnicianInfo.builder()
                .id(1L)
                .role("technician")
                .build();
        
        assertTrue(info.isTechnician());
    }
    
    @Test
    void testBuilderAndGetters() {
        TechnicianInfo info = TechnicianInfo.builder()
                .id(123L)
                .name("John Doe")
                .email("john.doe@test.com")
                .role("TECHNICIAN")
                .status("ACTIVE")
                .build();
        
        assertEquals(123L, info.getId());
        assertEquals("John Doe", info.getName());
        assertEquals("john.doe@test.com", info.getEmail());
        assertEquals("TECHNICIAN", info.getRole());
        assertEquals("ACTIVE", info.getStatus());
    }
}
