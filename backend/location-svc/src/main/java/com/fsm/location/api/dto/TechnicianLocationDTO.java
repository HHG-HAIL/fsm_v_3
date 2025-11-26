package com.fsm.location.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for technician location information returned by the GET /api/technicians/locations endpoint.
 * Contains technician identification, location data, and current status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianLocationDTO {
    
    /**
     * The technician's unique identifier
     */
    private Long technicianId;
    
    /**
     * The technician's name (currently placeholder, will be fetched from identity-svc in future)
     */
    private String name;
    
    /**
     * The technician's current status (available, busy, offline)
     * For now, we'll derive this from location freshness
     */
    private String status;
    
    /**
     * Current latitude
     */
    private Double latitude;
    
    /**
     * Current longitude
     */
    private Double longitude;
    
    /**
     * Location accuracy in meters
     */
    private Double accuracy;
    
    /**
     * Timestamp when location was recorded
     */
    private LocalDateTime timestamp;
    
    /**
     * Battery level percentage (0-100)
     */
    private Integer batteryLevel;
}
