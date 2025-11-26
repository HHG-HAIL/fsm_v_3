package com.fsm.location.service;

import com.fsm.location.api.dto.LocationUpdateRequest;
import com.fsm.location.api.dto.TechnicianLocationDTO;
import com.fsm.location.domain.model.TechnicianLocation;
import com.fsm.location.domain.repository.LocationRepository;
import com.fsm.location.infrastructure.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing technician location updates.
 * Implements business logic including rate limiting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {
    
    private final LocationRepository locationRepository;
    
    /**
     * Rate limiting threshold: minimum seconds between updates for the same technician.
     */
    private static final int RATE_LIMIT_SECONDS = 30;
    
    /**
     * Stale location threshold: locations older than this are considered stale (in minutes).
     */
    private static final int STALE_LOCATION_MINUTES = 15;
    
    /**
     * Updates a technician's location.
     * Enforces rate limiting: max once per 30 seconds per technician.
     * 
     * @param technicianId the ID of the technician
     * @param request the location update request
     * @return the saved location
     * @throws IllegalStateException if rate limit is exceeded
     */
    @Transactional
    public TechnicianLocation updateLocation(Long technicianId, LocationUpdateRequest request) {
        log.debug("Updating location for technician {}: lat={}, lon={}, accuracy={}", 
                  technicianId, request.getLatitude(), request.getLongitude(), request.getAccuracy());
        
        // Check rate limiting
        validateRateLimit(technicianId);
        
        // Create and save new location
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(technicianId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracy(request.getAccuracy())
                .batteryLevel(request.getBatteryLevel())
                .timestamp(LocalDateTime.now())
                .build();
        
        TechnicianLocation saved = locationRepository.save(location);
        
        log.info("Location updated for technician {}: locationId={}", technicianId, saved.getId());
        
        return saved;
    }
    
    /**
     * Validates that the technician hasn't exceeded the rate limit.
     * Rate limit: one update per 30 seconds.
     * 
     * @param technicianId the ID of the technician
     * @throws IllegalStateException if rate limit is exceeded
     */
    private void validateRateLimit(Long technicianId) {
        Optional<TechnicianLocation> latestLocation = 
                locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId);
        
        if (latestLocation.isPresent()) {
            LocalDateTime lastUpdate = latestLocation.get().getTimestamp();
            LocalDateTime rateLimitThreshold = LocalDateTime.now().minusSeconds(RATE_LIMIT_SECONDS);
            
            if (lastUpdate.isAfter(rateLimitThreshold)) {
                long secondsSinceLastUpdate = java.time.Duration.between(lastUpdate, LocalDateTime.now()).getSeconds();
                long secondsToWait = RATE_LIMIT_SECONDS - secondsSinceLastUpdate;
                
                log.warn("Rate limit exceeded for technician {}: last update was {} seconds ago", 
                         technicianId, secondsSinceLastUpdate);
                
                throw new IllegalStateException(
                        String.format("Rate limit exceeded. Please wait %d seconds before updating location again.", 
                                      secondsToWait));
            }
        }
    }
    
    /**
     * Gets the latest location for a technician.
     * 
     * @param technicianId the ID of the technician
     * @return the latest location if available
     */
    @Transactional(readOnly = true)
    public Optional<TechnicianLocation> getLatestLocation(Long technicianId) {
        return locationRepository.findFirstByTechnicianIdOrderByTimestampDesc(technicianId);
    }
    
    /**
     * Gets all active technician locations for map display.
     * Returns the latest location for each technician, filtering out stale locations (older than 15 minutes).
     * Results are cached for 30 seconds to reduce database load.
     * 
     * Domain Invariants:
     * - Only returns locations for active technicians (non-stale locations)
     * - Stale locations (older than 15 minutes) indicate technician may be offline
     * 
     * @return list of active technician locations with derived status
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ALL_LOCATIONS_CACHE)
    public List<TechnicianLocationDTO> getAllActiveTechnicianLocations() {
        log.debug("Fetching all active technician locations");
        
        // Get locations updated within the last 15 minutes (non-stale)
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(STALE_LOCATION_MINUTES);
        List<TechnicianLocation> activeLocations = locationRepository.findRecentLocations(staleThreshold);
        
        log.info("Found {} active technician locations", activeLocations.size());
        
        // Convert to DTOs with derived status
        return activeLocations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Converts a TechnicianLocation entity to a TechnicianLocationDTO.
     * Derives the status based on location freshness:
     * - "available" if location is recent (within 5 minutes)
     * - "busy" if location is active but not recent (5-15 minutes)
     * 
     * @param location the technician location entity
     * @return the DTO with derived status
     */
    private TechnicianLocationDTO convertToDTO(TechnicianLocation location) {
        // Derive status based on location freshness
        String status = location.isRecent() ? "available" : "busy";
        
        // For now, use placeholder for name. In future, this would be fetched from identity-svc
        String name = "Technician " + location.getTechnicianId();
        
        return TechnicianLocationDTO.builder()
                .technicianId(location.getTechnicianId())
                .name(name)
                .status(status)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .accuracy(location.getAccuracy())
                .timestamp(location.getTimestamp())
                .batteryLevel(location.getBatteryLevel())
                .build();
    }
}
