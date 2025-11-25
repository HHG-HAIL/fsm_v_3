package com.fsm.location.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating technician location.
 * Represents the UpdateLocation command from the mobile app.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationUpdateRequest {
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
    private Double longitude;
    
    @NotNull(message = "Accuracy is required")
    @Positive(message = "Accuracy must be positive")
    private Double accuracy;
    
    @Min(value = 0, message = "Battery level must be at least 0")
    @Max(value = 100, message = "Battery level must be at most 100")
    private Integer batteryLevel;
}
