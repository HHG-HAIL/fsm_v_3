package com.fsm.location.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * TechnicianLocation entity representing a technician's position in the FSM system.
 * This is the aggregate root for the Location Services bounded context.
 * 
 * Domain Invariants:
 * - Latitude must be between -90 and 90
 * - Longitude must be between -180 and 180
 * - Accuracy must be positive (in meters)
 * - Location timestamp must not be in future
 */
@Entity
@Table(name = "technician_locations", indexes = {
    @Index(name = "idx_technician_locations_technician_id", columnList = "technician_id"),
    @Index(name = "idx_technician_locations_timestamp", columnList = "timestamp"),
    @Index(name = "idx_technician_locations_tech_timestamp", columnList = "technician_id, timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Technician ID is required")
    @Column(name = "technician_id", nullable = false)
    private Long technicianId;
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
    @Column(nullable = false, precision = 10, scale = 7)
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
    @Column(nullable = false, precision = 10, scale = 7)
    private Double longitude;
    
    @NotNull(message = "Accuracy is required")
    @Positive(message = "Accuracy must be positive")
    @Column(nullable = false)
    private Double accuracy;
    
    @NotNull(message = "Timestamp is required")
    @PastOrPresent(message = "Timestamp must not be in the future")
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Min(value = 0, message = "Battery level must be at least 0")
    @Max(value = 100, message = "Battery level must be at most 100")
    @Column(name = "battery_level")
    private Integer batteryLevel;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    /**
     * Checks if this location is recent (within the last 5 minutes)
     * @return true if the location was recorded within the last 5 minutes
     */
    public boolean isRecent() {
        return timestamp != null && 
               timestamp.isAfter(LocalDateTime.now().minusMinutes(5));
    }
    
    /**
     * Checks if this location is stale (older than 15 minutes)
     * @return true if the location is older than 15 minutes
     */
    public boolean isStale() {
        return timestamp != null && 
               timestamp.isBefore(LocalDateTime.now().minusMinutes(15));
    }
    
    /**
     * Checks if the battery level is low (below 20%)
     * @return true if battery level is below 20%
     */
    public boolean isLowBattery() {
        return batteryLevel != null && batteryLevel < 20;
    }
    
    /**
     * Checks if the location accuracy is high (within 10 meters)
     * @return true if accuracy is 10 meters or better
     */
    public boolean isHighAccuracy() {
        return accuracy != null && accuracy <= 10.0;
    }
    
    /**
     * Calculates the distance in kilometers to another location using the Haversine formula
     * @param other the other location
     * @return distance in kilometers
     */
    public double distanceTo(TechnicianLocation other) {
        if (other == null || this.latitude == null || this.longitude == null ||
            other.getLatitude() == null || other.getLongitude() == null) {
            return Double.NaN;
        }
        
        final double R = 6371.0; // Earth's radius in kilometers
        
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.getLatitude());
        double deltaLat = Math.toRadians(other.getLatitude() - this.latitude);
        double deltaLon = Math.toRadians(other.getLongitude() - this.longitude);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
