package com.fsm.location.api.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocationUpdateResponse DTO.
 */
class LocationUpdateResponseTest {
    
    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        LocationUpdateResponse response = LocationUpdateResponse.builder()
                .locationId(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .timestamp(now)
                .message("Location updated successfully")
                .build();
        
        assertNotNull(response);
        assertEquals(1L, response.getLocationId());
        assertEquals(101L, response.getTechnicianId());
        assertEquals(39.7817, response.getLatitude());
        assertEquals(-89.6501, response.getLongitude());
        assertEquals(now, response.getTimestamp());
        assertEquals("Location updated successfully", response.getMessage());
    }
    
    @Test
    void testNoArgsConstructor() {
        LocationUpdateResponse response = new LocationUpdateResponse();
        assertNotNull(response);
    }
    
    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        LocationUpdateResponse response = new LocationUpdateResponse(
                1L, 101L, 39.7817, -89.6501, now, "Success");
        
        assertNotNull(response);
        assertEquals(1L, response.getLocationId());
        assertEquals(101L, response.getTechnicianId());
        assertEquals(39.7817, response.getLatitude());
        assertEquals(-89.6501, response.getLongitude());
        assertEquals(now, response.getTimestamp());
        assertEquals("Success", response.getMessage());
    }
    
    @Test
    void testGettersAndSetters() {
        LocationUpdateResponse response = new LocationUpdateResponse();
        LocalDateTime now = LocalDateTime.now();
        
        response.setLocationId(1L);
        response.setTechnicianId(101L);
        response.setLatitude(39.7817);
        response.setLongitude(-89.6501);
        response.setTimestamp(now);
        response.setMessage("Success");
        
        assertEquals(1L, response.getLocationId());
        assertEquals(101L, response.getTechnicianId());
        assertEquals(39.7817, response.getLatitude());
        assertEquals(-89.6501, response.getLongitude());
        assertEquals(now, response.getTimestamp());
        assertEquals("Success", response.getMessage());
    }
    
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        LocationUpdateResponse response1 = LocationUpdateResponse.builder()
                .locationId(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .timestamp(now)
                .message("Success")
                .build();
        
        LocationUpdateResponse response2 = LocationUpdateResponse.builder()
                .locationId(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .timestamp(now)
                .message("Success")
                .build();
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
    
    @Test
    void testToString() {
        LocationUpdateResponse response = LocationUpdateResponse.builder()
                .locationId(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .timestamp(LocalDateTime.now())
                .message("Success")
                .build();
        
        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("101"));
        assertTrue(toString.contains("Success"));
    }
}
