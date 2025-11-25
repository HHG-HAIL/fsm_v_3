package com.fsm.location.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocationUpdateRequest DTO.
 */
class LocationUpdateRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidLocationUpdateRequest() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }
    
    @Test
    void testValidLocationUpdateRequestWithoutBatteryLevel() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request without battery level should have no violations");
    }
    
    @Test
    void testNullLatitude() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(null)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }
    
    @Test
    void testLatitudeTooLow() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(-91.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }
    
    @Test
    void testLatitudeTooHigh() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(91.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }
    
    @Test
    void testValidLatitudeBoundaries() {
        // Test minimum valid latitude (-90)
        LocationUpdateRequest requestMin = LocationUpdateRequest.builder()
                .latitude(-90.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMin = validator.validate(requestMin);
        assertTrue(violationsMin.isEmpty() || violationsMin.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("latitude")));
        
        // Test maximum valid latitude (90)
        LocationUpdateRequest requestMax = LocationUpdateRequest.builder()
                .latitude(90.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMax = validator.validate(requestMax);
        assertTrue(violationsMax.isEmpty() || violationsMax.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }
    
    @Test
    void testNullLongitude() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(null)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }
    
    @Test
    void testLongitudeTooLow() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-181.0)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }
    
    @Test
    void testLongitudeTooHigh() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(181.0)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }
    
    @Test
    void testValidLongitudeBoundaries() {
        // Test minimum valid longitude (-180)
        LocationUpdateRequest requestMin = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-180.0)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMin = validator.validate(requestMin);
        assertTrue(violationsMin.isEmpty() || violationsMin.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("longitude")));
        
        // Test maximum valid longitude (180)
        LocationUpdateRequest requestMax = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(180.0)
                .accuracy(5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMax = validator.validate(requestMax);
        assertTrue(violationsMax.isEmpty() || violationsMax.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }
    
    @Test
    void testNullAccuracy() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(null)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accuracy")));
    }
    
    @Test
    void testNegativeAccuracy() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(-5.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accuracy")));
    }
    
    @Test
    void testZeroAccuracy() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(0.0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accuracy")));
    }
    
    @Test
    void testBatteryLevelTooLow() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(-1)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("batteryLevel")));
    }
    
    @Test
    void testBatteryLevelTooHigh() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(101)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("batteryLevel")));
    }
    
    @Test
    void testValidBatteryLevelBoundaries() {
        // Test minimum valid battery level (0)
        LocationUpdateRequest requestMin = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(0)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMin = validator.validate(requestMin);
        assertTrue(violationsMin.isEmpty() || violationsMin.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("batteryLevel")));
        
        // Test maximum valid battery level (100)
        LocationUpdateRequest requestMax = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(100)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMax = validator.validate(requestMax);
        assertTrue(violationsMax.isEmpty() || violationsMax.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("batteryLevel")));
    }
    
    @Test
    void testGettersAndSetters() {
        LocationUpdateRequest request = new LocationUpdateRequest();
        
        request.setLatitude(39.7817);
        request.setLongitude(-89.6501);
        request.setAccuracy(5.0);
        request.setBatteryLevel(85);
        
        assertEquals(39.7817, request.getLatitude());
        assertEquals(-89.6501, request.getLongitude());
        assertEquals(5.0, request.getAccuracy());
        assertEquals(85, request.getBatteryLevel());
    }
    
    @Test
    void testEqualsAndHashCode() {
        LocationUpdateRequest request1 = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .build();
        
        LocationUpdateRequest request2 = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .build();
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    void testToString() {
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .batteryLevel(85)
                .build();
        
        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("39.7817"));
        assertTrue(toString.contains("-89.6501"));
    }
}
