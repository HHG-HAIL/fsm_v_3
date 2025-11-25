package com.fsm.location.api.controller;

import com.fsm.location.api.dto.LocationUpdateRequest;
import com.fsm.location.api.dto.LocationUpdateResponse;
import com.fsm.location.domain.model.TechnicianLocation;
import com.fsm.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for technician location operations.
 * Provides endpoints for updating and retrieving technician locations.
 */
@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Location", description = "Technician Location Management APIs")
public class LocationController {
    
    private final LocationService locationService;
    
    /**
     * Updates the authenticated technician's location.
     * Rate limited to once per 30 seconds per technician.
     * 
     * @param technicianId the technician ID (from authentication)
     * @param request the location update request
     * @return the updated location response with 201 Created status
     */
    @PostMapping("/me/location")
    @Operation(
        summary = "Update technician location",
        description = "Updates the current technician's location. Rate limited to once per 30 seconds."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Location successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<LocationUpdateResponse> updateMyLocation(
            @Parameter(description = "Technician ID from authenticated user", required = true)
            @RequestHeader(value = "X-Technician-Id") Long technicianId,
            @Valid @RequestBody LocationUpdateRequest request) {
        
        log.info("Received location update request for technician {}", technicianId);
        
        try {
            TechnicianLocation location = locationService.updateLocation(technicianId, request);
            
            LocationUpdateResponse response = LocationUpdateResponse.builder()
                    .locationId(location.getId())
                    .technicianId(location.getTechnicianId())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .timestamp(location.getTimestamp())
                    .message("Location updated successfully")
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalStateException e) {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for technician {}: {}", technicianId, e.getMessage());
            
            LocationUpdateResponse errorResponse = LocationUpdateResponse.builder()
                    .technicianId(technicianId)
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }
    }
    
    /**
     * Gets the latest location for the authenticated technician.
     * 
     * @param technicianId the technician ID (from authentication)
     * @return the latest location if available
     */
    @GetMapping("/me/location")
    @Operation(
        summary = "Get my latest location",
        description = "Retrieves the most recent location for the authenticated technician."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "No location found for this technician")
    })
    public ResponseEntity<LocationUpdateResponse> getMyLatestLocation(
            @Parameter(description = "Technician ID from authenticated user", required = true)
            @RequestHeader(value = "X-Technician-Id") Long technicianId) {
        
        log.debug("Fetching latest location for technician {}", technicianId);
        
        return locationService.getLatestLocation(technicianId)
                .map(location -> {
                    LocationUpdateResponse response = LocationUpdateResponse.builder()
                            .locationId(location.getId())
                            .technicianId(location.getTechnicianId())
                            .latitude(location.getLatitude())
                            .longitude(location.getLongitude())
                            .timestamp(location.getTimestamp())
                            .message("Location retrieved successfully")
                            .build();
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
