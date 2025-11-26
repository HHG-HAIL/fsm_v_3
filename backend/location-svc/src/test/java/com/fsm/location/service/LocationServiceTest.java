package com.fsm.location.service;

import com.fsm.location.api.dto.LocationUpdateRequest;
import com.fsm.location.api.dto.TechnicianLocationDTO;
import com.fsm.location.domain.model.TechnicianLocation;
import com.fsm.location.domain.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LocationService.
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceTest {
    
    @Mock
    private LocationRepository locationRepository;
    
    @InjectMocks
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
    
    @Test
    void testUpdateLocationSuccess() {
        // Given
        Long technicianId = 101L;
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.empty());
        when(locationRepository.save(any(TechnicianLocation.class)))
                .thenReturn(savedLocation);
        
        // When
        TechnicianLocation result = locationService.updateLocation(technicianId, validRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(savedLocation.getId(), result.getId());
        assertEquals(savedLocation.getTechnicianId(), result.getTechnicianId());
        assertEquals(savedLocation.getLatitude(), result.getLatitude());
        assertEquals(savedLocation.getLongitude(), result.getLongitude());
        
        verify(locationRepository).findFirstByTechnicianIdOrderByTimestampDesc(technicianId);
        verify(locationRepository).save(any(TechnicianLocation.class));
    }
    
    @Test
    void testUpdateLocationSavesCorrectData() {
        // Given
        Long technicianId = 101L;
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.empty());
        when(locationRepository.save(any(TechnicianLocation.class)))
                .thenReturn(savedLocation);
        
        // When
        locationService.updateLocation(technicianId, validRequest);
        
        // Then
        ArgumentCaptor<TechnicianLocation> captor = ArgumentCaptor.forClass(TechnicianLocation.class);
        verify(locationRepository).save(captor.capture());
        
        TechnicianLocation captured = captor.getValue();
        assertEquals(technicianId, captured.getTechnicianId());
        assertEquals(validRequest.getLatitude(), captured.getLatitude());
        assertEquals(validRequest.getLongitude(), captured.getLongitude());
        assertEquals(validRequest.getAccuracy(), captured.getAccuracy());
        assertEquals(validRequest.getBatteryLevel(), captured.getBatteryLevel());
        assertNotNull(captured.getTimestamp());
    }
    
    @Test
    void testUpdateLocationRateLimitNotExceeded() {
        // Given - last update was 31 seconds ago (should be allowed)
        Long technicianId = 101L;
        TechnicianLocation oldLocation = TechnicianLocation.builder()
                .id(1L)
                .technicianId(technicianId)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now().minusSeconds(31))
                .build();
        
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.of(oldLocation));
        when(locationRepository.save(any(TechnicianLocation.class)))
                .thenReturn(savedLocation);
        
        // When
        TechnicianLocation result = locationService.updateLocation(technicianId, validRequest);
        
        // Then
        assertNotNull(result);
        verify(locationRepository).save(any(TechnicianLocation.class));
    }
    
    @Test
    void testUpdateLocationRateLimitExceeded() {
        // Given - last update was 10 seconds ago (should be blocked)
        Long technicianId = 101L;
        TechnicianLocation recentLocation = TechnicianLocation.builder()
                .id(1L)
                .technicianId(technicianId)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now().minusSeconds(10))
                .build();
        
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.of(recentLocation));
        
        // When / Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> locationService.updateLocation(technicianId, validRequest));
        
        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
        assertTrue(exception.getMessage().contains("wait"));
        verify(locationRepository, never()).save(any(TechnicianLocation.class));
    }
    
    @Test
    void testUpdateLocationRateLimitJustUnder30Seconds() {
        // Given - last update was 29 seconds ago (should be blocked)
        Long technicianId = 101L;
        TechnicianLocation recentLocation = TechnicianLocation.builder()
                .id(1L)
                .technicianId(technicianId)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now().minusSeconds(29))
                .build();
        
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.of(recentLocation));
        
        // When / Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> locationService.updateLocation(technicianId, validRequest));
        
        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
        verify(locationRepository, never()).save(any(TechnicianLocation.class));
    }
    
    @Test
    void testUpdateLocationWithoutBatteryLevel() {
        // Given
        Long technicianId = 101L;
        LocationUpdateRequest requestWithoutBattery = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.empty());
        when(locationRepository.save(any(TechnicianLocation.class)))
                .thenReturn(savedLocation);
        
        // When
        TechnicianLocation result = locationService.updateLocation(technicianId, requestWithoutBattery);
        
        // Then
        assertNotNull(result);
        verify(locationRepository).save(any(TechnicianLocation.class));
    }
    
    @Test
    void testGetLatestLocationExists() {
        // Given
        Long technicianId = 101L;
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.of(savedLocation));
        
        // When
        Optional<TechnicianLocation> result = locationService.getLatestLocation(technicianId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(savedLocation.getId(), result.get().getId());
        verify(locationRepository).findFirstByTechnicianIdOrderByTimestampDesc(technicianId);
    }
    
    @Test
    void testGetLatestLocationNotExists() {
        // Given
        Long technicianId = 101L;
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.empty());
        
        // When
        Optional<TechnicianLocation> result = locationService.getLatestLocation(technicianId);
        
        // Then
        assertFalse(result.isPresent());
        verify(locationRepository).findFirstByTechnicianIdOrderByTimestampDesc(technicianId);
    }
    
    @Test
    void testUpdateLocationForDifferentTechnicians() {
        // Given - two different technicians
        Long technicianId1 = 101L;
        Long technicianId2 = 102L;
        
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId1))
                .thenReturn(Optional.empty());
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId2))
                .thenReturn(Optional.empty());
        when(locationRepository.save(any(TechnicianLocation.class)))
                .thenReturn(savedLocation);
        
        // When
        TechnicianLocation result1 = locationService.updateLocation(technicianId1, validRequest);
        TechnicianLocation result2 = locationService.updateLocation(technicianId2, validRequest);
        
        // Then - both should succeed (rate limit is per technician)
        assertNotNull(result1);
        assertNotNull(result2);
        verify(locationRepository, times(2)).save(any(TechnicianLocation.class));
    }
    
    @Test
    void testUpdateLocationCreatesTimestamp() {
        // Given
        Long technicianId = 101L;
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.empty());
        when(locationRepository.save(any(TechnicianLocation.class)))
                .thenReturn(savedLocation);
        
        // When
        locationService.updateLocation(technicianId, validRequest);
        
        // Then
        ArgumentCaptor<TechnicianLocation> captor = ArgumentCaptor.forClass(TechnicianLocation.class);
        verify(locationRepository).save(captor.capture());
        
        TechnicianLocation captured = captor.getValue();
        assertNotNull(captured.getTimestamp());
        assertTrue(captured.getTimestamp().isAfter(beforeUpdate));
    }
    
    @Test
    void testUpdateLocationWithVariousCoordinates() {
        // Given - test various valid coordinate combinations
        Long technicianId = 101L;
        
        // Test case 1: North Pole
        LocationUpdateRequest northPole = LocationUpdateRequest.builder()
                .latitude(90.0)
                .longitude(0.0)
                .accuracy(10.0)
                .build();
        
        // Test case 2: South Pole
        LocationUpdateRequest southPole = LocationUpdateRequest.builder()
                .latitude(-90.0)
                .longitude(0.0)
                .accuracy(10.0)
                .build();
        
        // Test case 3: International Date Line
        LocationUpdateRequest dateLine = LocationUpdateRequest.builder()
                .latitude(0.0)
                .longitude(180.0)
                .accuracy(10.0)
                .build();
        
        when(locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId))
                .thenReturn(Optional.empty());
        when(locationRepository.save(any(TechnicianLocation.class)))
                .thenReturn(savedLocation);
        
        // When / Then - all should succeed
        assertDoesNotThrow(() -> locationService.updateLocation(technicianId, northPole));
        assertDoesNotThrow(() -> locationService.updateLocation(technicianId, southPole));
        assertDoesNotThrow(() -> locationService.updateLocation(technicianId, dateLine));
    }
    
    @Test
    void testGetAllActiveTechnicianLocationsReturnsActiveLocations() {
        // Given - locations updated within the last 15 minutes
        LocalDateTime now = LocalDateTime.now();
        TechnicianLocation location1 = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now.minusMinutes(2))
                .batteryLevel(85)
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .id(2L)
                .technicianId(102L)
                .latitude(39.7845)
                .longitude(-89.6302)
                .accuracy(8.0)
                .timestamp(now.minusMinutes(7))
                .batteryLevel(62)
                .build();
        
        when(locationRepository.findRecentLocations(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(location1, location2));
        
        // When
        List<TechnicianLocationDTO> result = locationService.getAllActiveTechnicianLocations();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first location
        TechnicianLocationDTO dto1 = result.get(0);
        assertEquals(101L, dto1.getTechnicianId());
        assertEquals("Technician 101", dto1.getName());
        assertEquals("available", dto1.getStatus()); // Recent location (< 5 min)
        assertEquals(39.7817, dto1.getLatitude());
        assertEquals(-89.6501, dto1.getLongitude());
        assertEquals(5.0, dto1.getAccuracy());
        assertEquals(85, dto1.getBatteryLevel());
        
        // Verify second location
        TechnicianLocationDTO dto2 = result.get(1);
        assertEquals(102L, dto2.getTechnicianId());
        assertEquals("Technician 102", dto2.getName());
        assertEquals("busy", dto2.getStatus()); // Not recent (> 5 min but < 15 min)
        assertEquals(39.7845, dto2.getLatitude());
        assertEquals(-89.6302, dto2.getLongitude());
        assertEquals(8.0, dto2.getAccuracy());
        assertEquals(62, dto2.getBatteryLevel());
        
        verify(locationRepository).findRecentLocations(any(LocalDateTime.class));
    }
    
    @Test
    void testGetAllActiveTechnicianLocationsEmptyList() {
        // Given
        when(locationRepository.findRecentLocations(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        
        // When
        List<TechnicianLocationDTO> result = locationService.getAllActiveTechnicianLocations();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(locationRepository).findRecentLocations(any(LocalDateTime.class));
    }
    
    @Test
    void testGetAllActiveTechnicianLocationsFiltersStaleLocations() {
        // Given - verify that the service requests locations from the correct threshold
        ArgumentCaptor<LocalDateTime> thresholdCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(locationRepository.findRecentLocations(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        
        LocalDateTime beforeCall = LocalDateTime.now().minusMinutes(15);
        
        // When
        locationService.getAllActiveTechnicianLocations();
        
        // Then
        verify(locationRepository).findRecentLocations(thresholdCaptor.capture());
        LocalDateTime capturedThreshold = thresholdCaptor.getValue();
        
        // Verify threshold is approximately 15 minutes ago (with 1 second tolerance)
        assertTrue(capturedThreshold.isAfter(beforeCall.minusSeconds(1)));
        assertTrue(capturedThreshold.isBefore(beforeCall.plusSeconds(2)));
    }
    
    @Test
    void testGetAllActiveTechnicianLocationsMultipleTechnicians() {
        // Given - multiple technicians with various locations
        LocalDateTime now = LocalDateTime.now();
        List<TechnicianLocation> locations = Arrays.asList(
                TechnicianLocation.builder()
                        .id(1L).technicianId(101L)
                        .latitude(39.7817).longitude(-89.6501)
                        .accuracy(5.0).timestamp(now.minusMinutes(1))
                        .batteryLevel(85).build(),
                TechnicianLocation.builder()
                        .id(2L).technicianId(102L)
                        .latitude(39.7845).longitude(-89.6302)
                        .accuracy(8.0).timestamp(now.minusMinutes(3))
                        .batteryLevel(62).build(),
                TechnicianLocation.builder()
                        .id(3L).technicianId(103L)
                        .latitude(39.7789).longitude(-89.6720)
                        .accuracy(12.0).timestamp(now.minusMinutes(10))
                        .batteryLevel(45).build()
        );
        
        when(locationRepository.findRecentLocations(any(LocalDateTime.class)))
                .thenReturn(locations);
        
        // When
        List<TechnicianLocationDTO> result = locationService.getAllActiveTechnicianLocations();
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify all technicians are included with correct statuses
        assertEquals("available", result.get(0).getStatus()); // 1 min ago
        assertEquals("available", result.get(1).getStatus()); // 3 min ago
        assertEquals("busy", result.get(2).getStatus()); // 10 min ago
    }
    
    @Test
    void testConvertToDTOSetsCorrectStatus() {
        // Given - test boundary cases for status determination
        LocalDateTime now = LocalDateTime.now();
        
        // Location exactly 5 minutes ago (boundary - should be "busy")
        TechnicianLocation location5MinAgo = TechnicianLocation.builder()
                .id(1L).technicianId(101L)
                .latitude(39.7817).longitude(-89.6501)
                .accuracy(5.0).timestamp(now.minusMinutes(5).minusSeconds(1))
                .batteryLevel(85).build();
        
        // Location just under 5 minutes ago (should be "available")
        TechnicianLocation locationJustUnder5Min = TechnicianLocation.builder()
                .id(2L).technicianId(102L)
                .latitude(39.7845).longitude(-89.6302)
                .accuracy(8.0).timestamp(now.minusMinutes(4).minusSeconds(59))
                .batteryLevel(62).build();
        
        when(locationRepository.findRecentLocations(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(location5MinAgo, locationJustUnder5Min));
        
        // When
        List<TechnicianLocationDTO> result = locationService.getAllActiveTechnicianLocations();
        
        // Then
        assertEquals(2, result.size());
        assertEquals("busy", result.get(0).getStatus());
        assertEquals("available", result.get(1).getStatus());
    }
}
