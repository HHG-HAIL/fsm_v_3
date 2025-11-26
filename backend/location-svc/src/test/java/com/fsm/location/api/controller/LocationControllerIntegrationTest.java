package com.fsm.location.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.location.api.dto.LocationUpdateRequest;
import com.fsm.location.domain.model.TechnicianLocation;
import com.fsm.location.domain.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for LocationController.
 * Tests the complete flow from HTTP request to database persistence.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocationControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @BeforeEach
    void setUp() {
        // Clean up database before each test
        locationRepository.deleteAll();
    }
    
    @WithMockUser
    @Test
    void testUpdateLocationSuccessfullyPersistsToDatabase() throws Exception {
        // Given
        Long technicianId = 101L;
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .build();
        
        // When
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locationId").exists())
                .andExpect(jsonPath("$.technicianId").value(technicianId))
                .andExpect(jsonPath("$.latitude").value(39.7817))
                .andExpect(jsonPath("$.longitude").value(-89.6501))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Location updated successfully"));
        
        // Then - verify database persistence
        List<TechnicianLocation> locations = locationRepository.findByTechnicianIdOrderByTimestampDesc(technicianId);
        assertEquals(1, locations.size());
        
        TechnicianLocation savedLocation = locations.get(0);
        assertEquals(technicianId, savedLocation.getTechnicianId());
        assertEquals(39.7817, savedLocation.getLatitude());
        assertEquals(-89.6501, savedLocation.getLongitude());
        assertEquals(5.0, savedLocation.getAccuracy());
        assertEquals(85, savedLocation.getBatteryLevel());
        assertNotNull(savedLocation.getTimestamp());
        assertNotNull(savedLocation.getCreatedAt());
    }
    
    @WithMockUser
    @Test
    void testUpdateLocationEnforcesRateLimiting() throws Exception {
        // Given - first location update
        Long technicianId = 102L;
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .build();
        
        // When - first update succeeds
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // When - immediate second update (should fail due to rate limiting)
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Rate limit exceeded")));
        
        // Then - verify only one location was saved
        List<TechnicianLocation> locations = locationRepository.findByTechnicianIdOrderByTimestampDesc(technicianId);
        assertEquals(1, locations.size());
    }
    
    @WithMockUser
    @Test
    void testGetLatestLocationReturnsPersistedData() throws Exception {
        // Given - save a location first
        Long technicianId = 103L;
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(technicianId)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .timestamp(LocalDateTime.now())
                .build();
        locationRepository.save(location);
        
        // When
        mockMvc.perform(get("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId").exists())
                .andExpect(jsonPath("$.technicianId").value(technicianId))
                .andExpect(jsonPath("$.latitude").value(39.7817))
                .andExpect(jsonPath("$.longitude").value(-89.6501))
                .andExpect(jsonPath("$.message").value("Location retrieved successfully"));
    }
    
    @WithMockUser
    @Test
    void testGetLatestLocationReturnsNotFoundWhenNoLocation() throws Exception {
        // Given - no location exists for this technician
        Long technicianId = 104L;
        
        // When / Then
        mockMvc.perform(get("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId.toString()))
                .andExpect(status().isNotFound());
    }
    
    @WithMockUser
    @Test
    void testUpdateLocationValidatesCoordinates() throws Exception {
        // Given - invalid latitude
        Long technicianId = 105L;
        LocationUpdateRequest invalidRequest = LocationUpdateRequest.builder()
                .latitude(91.0) // Invalid - exceeds max
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        // Verify nothing was saved
        List<TechnicianLocation> locations = locationRepository.findByTechnicianIdOrderByTimestampDesc(technicianId);
        assertEquals(0, locations.size());
    }
    
    @WithMockUser
    @Test
    void testMultipleTechniciansCanUpdateIndependently() throws Exception {
        // Given - two different technicians
        Long technicianId1 = 106L;
        Long technicianId2 = 107L;
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .build();
        
        // When - both technicians update their locations
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId1.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId2.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Then - verify both locations were saved
        List<TechnicianLocation> locations1 = locationRepository.findByTechnicianIdOrderByTimestampDesc(technicianId1);
        List<TechnicianLocation> locations2 = locationRepository.findByTechnicianIdOrderByTimestampDesc(technicianId2);
        
        assertEquals(1, locations1.size());
        assertEquals(1, locations2.size());
        assertEquals(technicianId1, locations1.get(0).getTechnicianId());
        assertEquals(technicianId2, locations2.get(0).getTechnicianId());
    }
    
    @WithMockUser
    @Test
    void testUpdateLocationWithBoundaryValues() throws Exception {
        // Given - boundary latitude and longitude values
        Long technicianId = 108L;
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(-90.0) // Minimum valid latitude
                .longitude(-180.0) // Minimum valid longitude
                .accuracy(0.001) // Very small accuracy
                .batteryLevel(0) // Minimum battery level
                .build();
        
        // When
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Then
        List<TechnicianLocation> locations = locationRepository.findByTechnicianIdOrderByTimestampDesc(technicianId);
        assertEquals(1, locations.size());
        assertEquals(-90.0, locations.get(0).getLatitude());
        assertEquals(-180.0, locations.get(0).getLongitude());
        assertEquals(0, locations.get(0).getBatteryLevel());
    }
    
    @WithMockUser
    @Test
    void testUpdateLocationWithoutBatteryLevel() throws Exception {
        // Given - request without battery level
        Long technicianId = 109L;
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                // No battery level
                .build();
        
        // When
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", technicianId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Then
        List<TechnicianLocation> locations = locationRepository.findByTechnicianIdOrderByTimestampDesc(technicianId);
        assertEquals(1, locations.size());
        assertNull(locations.get(0).getBatteryLevel());
    }
}
