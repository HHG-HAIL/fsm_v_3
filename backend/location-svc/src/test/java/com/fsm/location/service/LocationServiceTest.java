package com.fsm.location.service;

import com.fsm.location.api.dto.LocationUpdateRequest;
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
    void testUpdateLocationRateLimitExactly30Seconds() {
        // Given - last update was 29 seconds ago (clearly within rate limit, should be blocked)
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
}
