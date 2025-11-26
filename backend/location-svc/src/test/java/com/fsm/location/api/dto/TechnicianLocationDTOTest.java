package com.fsm.location.api.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TechnicianLocationDTO.
 */
class TechnicianLocationDTOTest {
    
    @Test
    void testBuilderCreatesValidDTO() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        
        // When
        TechnicianLocationDTO dto = TechnicianLocationDTO.builder()
                .technicianId(101L)
                .name("John Doe")
                .status("available")
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(timestamp)
                .batteryLevel(85)
                .build();
        
        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getTechnicianId()).isEqualTo(101L);
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getStatus()).isEqualTo("available");
        assertThat(dto.getLatitude()).isEqualTo(39.7817);
        assertThat(dto.getLongitude()).isEqualTo(-89.6501);
        assertThat(dto.getAccuracy()).isEqualTo(5.0);
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getBatteryLevel()).isEqualTo(85);
    }
    
    @Test
    void testNoArgsConstructor() {
        // When
        TechnicianLocationDTO dto = new TechnicianLocationDTO();
        
        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getTechnicianId()).isNull();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getLatitude()).isNull();
        assertThat(dto.getLongitude()).isNull();
        assertThat(dto.getAccuracy()).isNull();
        assertThat(dto.getTimestamp()).isNull();
        assertThat(dto.getBatteryLevel()).isNull();
    }
    
    @Test
    void testAllArgsConstructor() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        
        // When
        TechnicianLocationDTO dto = new TechnicianLocationDTO(
                101L, "John Doe", "busy", 39.7817, -89.6501, 5.0, timestamp, 85
        );
        
        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getTechnicianId()).isEqualTo(101L);
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getStatus()).isEqualTo("busy");
        assertThat(dto.getLatitude()).isEqualTo(39.7817);
        assertThat(dto.getLongitude()).isEqualTo(-89.6501);
        assertThat(dto.getAccuracy()).isEqualTo(5.0);
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getBatteryLevel()).isEqualTo(85);
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        TechnicianLocationDTO dto = new TechnicianLocationDTO();
        LocalDateTime timestamp = LocalDateTime.now();
        
        // When
        dto.setTechnicianId(102L);
        dto.setName("Jane Smith");
        dto.setStatus("available");
        dto.setLatitude(40.0);
        dto.setLongitude(-90.0);
        dto.setAccuracy(10.0);
        dto.setTimestamp(timestamp);
        dto.setBatteryLevel(70);
        
        // Then
        assertThat(dto.getTechnicianId()).isEqualTo(102L);
        assertThat(dto.getName()).isEqualTo("Jane Smith");
        assertThat(dto.getStatus()).isEqualTo("available");
        assertThat(dto.getLatitude()).isEqualTo(40.0);
        assertThat(dto.getLongitude()).isEqualTo(-90.0);
        assertThat(dto.getAccuracy()).isEqualTo(10.0);
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getBatteryLevel()).isEqualTo(70);
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        TechnicianLocationDTO dto1 = TechnicianLocationDTO.builder()
                .technicianId(101L)
                .name("John Doe")
                .status("available")
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(timestamp)
                .batteryLevel(85)
                .build();
        
        TechnicianLocationDTO dto2 = TechnicianLocationDTO.builder()
                .technicianId(101L)
                .name("John Doe")
                .status("available")
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(timestamp)
                .batteryLevel(85)
                .build();
        
        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }
    
    @Test
    void testToString() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        TechnicianLocationDTO dto = TechnicianLocationDTO.builder()
                .technicianId(101L)
                .name("John Doe")
                .status("available")
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(timestamp)
                .batteryLevel(85)
                .build();
        
        // When
        String toString = dto.toString();
        
        // Then
        assertThat(toString).contains("technicianId=101");
        assertThat(toString).contains("name=John Doe");
        assertThat(toString).contains("status=available");
    }
}
