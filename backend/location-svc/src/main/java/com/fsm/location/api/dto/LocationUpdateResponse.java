package com.fsm.location.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for location update operation.
 * Confirms successful location update with timestamp.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationUpdateResponse {
    
    private Long locationId;
    private Long technicianId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private String message;
}
