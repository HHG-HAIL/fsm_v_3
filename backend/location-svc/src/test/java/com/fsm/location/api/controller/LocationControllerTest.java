package com.fsm.location.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.location.api.dto.LocationUpdateRequest;
import com.fsm.location.api.dto.TechnicianLocationDTO;
import com.fsm.location.domain.model.TechnicianLocation;
import com.fsm.location.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for LocationController.
 */
@WebMvcTest(controllers = LocationController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        })
class LocationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private LocationService locationService;
    
    private LocationUpdateRequest validRequest;
    private TechnicianLocation savedLocation;
    
    @BeforeEach
    void setUp() {
        validRequest = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .build();
        
        savedLocation = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .timestamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationSuccess() throws Exception {
        // Given
        when(locationService.updateLocation(eq(101L), any(LocationUpdateRequest.class)))
                .thenReturn(savedLocation);
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locationId").value(1))
                .andExpect(jsonPath("$.technicianId").value(101))
                .andExpect(jsonPath("$.latitude").value(39.7817))
                .andExpect(jsonPath("$.longitude").value(-89.6501))
                .andExpect(jsonPath("$.message").value("Location updated successfully"));
        
        verify(locationService).updateLocation(eq(101L), any(LocationUpdateRequest.class));
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationRateLimitExceeded() throws Exception {
        // Given
        when(locationService.updateLocation(eq(101L), any(LocationUpdateRequest.class)))
                .thenThrow(new IllegalStateException("Rate limit exceeded. Please wait 20 seconds before updating location again."));
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.technicianId").value(101))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Please wait 20 seconds before updating location again."));
        
        verify(locationService).updateLocation(eq(101L), any(LocationUpdateRequest.class));
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationMissingLatitude() throws Exception {
        // Given
        LocationUpdateRequest invalidRequest = LocationUpdateRequest.builder()
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(locationService, never()).updateLocation(any(), any());
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationMissingLongitude() throws Exception {
        // Given
        LocationUpdateRequest invalidRequest = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .accuracy(5.0)
                .build();
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(locationService, never()).updateLocation(any(), any());
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationMissingAccuracy() throws Exception {
        // Given
        LocationUpdateRequest invalidRequest = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .build();
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(locationService, never()).updateLocation(any(), any());
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationInvalidLatitude() throws Exception {
        // Given
        LocationUpdateRequest invalidRequest = LocationUpdateRequest.builder()
                .latitude(91.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(locationService, never()).updateLocation(any(), any());
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationInvalidLongitude() throws Exception {
        // Given
        LocationUpdateRequest invalidRequest = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(181.0)
                .accuracy(5.0)
                .build();
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(locationService, never()).updateLocation(any(), any());
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationInvalidAccuracy() throws Exception {
        // Given
        LocationUpdateRequest invalidRequest = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(-5.0)
                .build();
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(locationService, never()).updateLocation(any(), any());
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationInvalidBatteryLevel() throws Exception {
        // Given
        LocationUpdateRequest invalidRequest = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(101)
                .build();
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(locationService, never()).updateLocation(any(), any());
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationWithoutBatteryLevel() throws Exception {
        // Given
        LocationUpdateRequest requestWithoutBattery = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        when(locationService.updateLocation(eq(101L), any(LocationUpdateRequest.class)))
                .thenReturn(savedLocation);
        
        // When / Then
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutBattery)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locationId").value(1))
                .andExpect(jsonPath("$.technicianId").value(101));
        
        verify(locationService).updateLocation(eq(101L), any(LocationUpdateRequest.class));
    }
    
    @WithMockUser
    @Test
    void testGetMyLatestLocationExists() throws Exception {
        // Given
        when(locationService.getLatestLocation(101L))
                .thenReturn(Optional.of(savedLocation));
        
        // When / Then
        mockMvc.perform(get("/api/technicians/me/location")
                        .header("X-Technician-Id", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId").value(1))
                .andExpect(jsonPath("$.technicianId").value(101))
                .andExpect(jsonPath("$.latitude").value(39.7817))
                .andExpect(jsonPath("$.longitude").value(-89.6501))
                .andExpect(jsonPath("$.message").value("Location retrieved successfully"));
        
        verify(locationService).getLatestLocation(101L);
    }
    
    @WithMockUser
    @Test
    void testGetMyLatestLocationNotFound() throws Exception {
        // Given
        when(locationService.getLatestLocation(101L))
                .thenReturn(Optional.empty());
        
        // When / Then
        mockMvc.perform(get("/api/technicians/me/location")
                        .header("X-Technician-Id", "101"))
                .andExpect(status().isNotFound());
        
        verify(locationService).getLatestLocation(101L);
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationValidBoundaryLatitudes() throws Exception {
        // Test minimum valid latitude (-90)
        LocationUpdateRequest minLatRequest = LocationUpdateRequest.builder()
                .latitude(-90.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        when(locationService.updateLocation(eq(101L), any(LocationUpdateRequest.class)))
                .thenReturn(savedLocation);
        
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minLatRequest)))
                .andExpect(status().isCreated());
        
        // Test maximum valid latitude (90)
        LocationUpdateRequest maxLatRequest = LocationUpdateRequest.builder()
                .latitude(90.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maxLatRequest)))
                .andExpect(status().isCreated());
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationValidBoundaryLongitudes() throws Exception {
        // Test minimum valid longitude (-180)
        LocationUpdateRequest minLonRequest = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-180.0)
                .accuracy(5.0)
                .build();
        
        when(locationService.updateLocation(eq(101L), any(LocationUpdateRequest.class)))
                .thenReturn(savedLocation);
        
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minLonRequest)))
                .andExpect(status().isCreated());
        
        // Test maximum valid longitude (180)
        LocationUpdateRequest maxLonRequest = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(180.0)
                .accuracy(5.0)
                .build();
        
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maxLonRequest)))
                .andExpect(status().isCreated());
    }
    
    @WithMockUser
    @Test
    void testUpdateMyLocationDifferentTechnicians() throws Exception {
        // Given
        TechnicianLocation location1 = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .id(2L)
                .technicianId(102L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        when(locationService.updateLocation(eq(101L), any(LocationUpdateRequest.class)))
                .thenReturn(location1);
        when(locationService.updateLocation(eq(102L), any(LocationUpdateRequest.class)))
                .thenReturn(location2);
        
        // When / Then - both should succeed
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technicianId").value(101));
        
        mockMvc.perform(post("/api/technicians/me/location")
                        .header("X-Technician-Id", "102")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technicianId").value(102));
    }
    
    @Test
    @WithMockUser(authorities = {"DISPATCHER"})
    void testGetAllTechnicianLocationsAsDispatcher() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<TechnicianLocationDTO> locations = Arrays.asList(
                TechnicianLocationDTO.builder()
                        .technicianId(101L)
                        .name("Technician 101")
                        .status("available")
                        .latitude(39.7817)
                        .longitude(-89.6501)
                        .accuracy(5.0)
                        .timestamp(now.minusMinutes(2))
                        .batteryLevel(85)
                        .build(),
                TechnicianLocationDTO.builder()
                        .technicianId(102L)
                        .name("Technician 102")
                        .status("busy")
                        .latitude(39.7845)
                        .longitude(-89.6302)
                        .accuracy(8.0)
                        .timestamp(now.minusMinutes(7))
                        .batteryLevel(62)
                        .build()
        );
        
        when(locationService.getAllActiveTechnicianLocations()).thenReturn(locations);
        
        // When / Then
        mockMvc.perform(get("/api/technicians/locations")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].technicianId").value(101))
                .andExpect(jsonPath("$[0].name").value("Technician 101"))
                .andExpect(jsonPath("$[0].status").value("available"))
                .andExpect(jsonPath("$[0].latitude").value(39.7817))
                .andExpect(jsonPath("$[0].longitude").value(-89.6501))
                .andExpect(jsonPath("$[0].accuracy").value(5.0))
                .andExpect(jsonPath("$[0].batteryLevel").value(85))
                .andExpect(jsonPath("$[1].technicianId").value(102))
                .andExpect(jsonPath("$[1].status").value("busy"));
        
        verify(locationService).getAllActiveTechnicianLocations();
    }
    
    @Test
    @WithMockUser(authorities = {"SUPERVISOR"})
    void testGetAllTechnicianLocationsAsSupervisor() throws Exception {
        // Given
        List<TechnicianLocationDTO> locations = Collections.singletonList(
                TechnicianLocationDTO.builder()
                        .technicianId(101L)
                        .name("Technician 101")
                        .status("available")
                        .latitude(39.7817)
                        .longitude(-89.6501)
                        .accuracy(5.0)
                        .timestamp(LocalDateTime.now())
                        .batteryLevel(85)
                        .build()
        );
        
        when(locationService.getAllActiveTechnicianLocations()).thenReturn(locations);
        
        // When / Then
        mockMvc.perform(get("/api/technicians/locations")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        
        verify(locationService).getAllActiveTechnicianLocations();
    }
    
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testGetAllTechnicianLocationsAsAdmin() throws Exception {
        // Given
        List<TechnicianLocationDTO> locations = Collections.singletonList(
                TechnicianLocationDTO.builder()
                        .technicianId(101L)
                        .name("Technician 101")
                        .status("available")
                        .latitude(39.7817)
                        .longitude(-89.6501)
                        .accuracy(5.0)
                        .timestamp(LocalDateTime.now())
                        .batteryLevel(85)
                        .build()
        );
        
        when(locationService.getAllActiveTechnicianLocations()).thenReturn(locations);
        
        // When / Then
        mockMvc.perform(get("/api/technicians/locations")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        
        verify(locationService).getAllActiveTechnicianLocations();
    }
    
    // Note: RBAC tests (TECHNICIAN forbidden, Unauthenticated access) are covered in LocationControllerIntegrationTest
    // because they require full security context which is not available in @WebMvcTest with excludeAutoConfiguration
    
    @Test
    @WithMockUser(authorities = {"DISPATCHER"})
    void testGetAllTechnicianLocationsEmptyList() throws Exception {
        // Given
        when(locationService.getAllActiveTechnicianLocations()).thenReturn(Collections.emptyList());
        
        // When / Then
        mockMvc.perform(get("/api/technicians/locations")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(locationService).getAllActiveTechnicianLocations();
    }
    
    @Test
    @WithMockUser(authorities = {"DISPATCHER"})
    void testGetAllTechnicianLocationsMultipleTechnicians() throws Exception {
        // Given - multiple technicians with various statuses
        LocalDateTime now = LocalDateTime.now();
        List<TechnicianLocationDTO> locations = Arrays.asList(
                TechnicianLocationDTO.builder()
                        .technicianId(101L).name("Technician 101").status("available")
                        .latitude(39.7817).longitude(-89.6501).accuracy(5.0)
                        .timestamp(now.minusMinutes(1)).batteryLevel(85).build(),
                TechnicianLocationDTO.builder()
                        .technicianId(102L).name("Technician 102").status("available")
                        .latitude(39.7845).longitude(-89.6302).accuracy(8.0)
                        .timestamp(now.minusMinutes(3)).batteryLevel(62).build(),
                TechnicianLocationDTO.builder()
                        .technicianId(103L).name("Technician 103").status("busy")
                        .latitude(39.7789).longitude(-89.6720).accuracy(12.0)
                        .timestamp(now.minusMinutes(10)).batteryLevel(45).build()
        );
        
        when(locationService.getAllActiveTechnicianLocations()).thenReturn(locations);
        
        // When / Then
        mockMvc.perform(get("/api/technicians/locations")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].status").value("available"))
                .andExpect(jsonPath("$[1].status").value("available"))
                .andExpect(jsonPath("$[2].status").value("busy"));
        
        verify(locationService).getAllActiveTechnicianLocations();
    }
}
