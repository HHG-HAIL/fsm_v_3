package com.fsm.task.application.service;

import com.fsm.task.application.dto.TechnicianInfo;
import com.fsm.task.application.exception.TechnicianNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for validating technicians by calling the identity-svc.
 * Checks if a technician exists and is active before allowing task assignment.
 */
@Service
@Slf4j
public class TechnicianValidationService {
    
    private final RestTemplate restTemplate;
    private final String identityServiceUrl;
    private final boolean validationEnabled;
    
    public TechnicianValidationService(
            RestTemplate restTemplate,
            @Value("${identity.service.url:http://localhost:8080}") String identityServiceUrl,
            @Value("${identity.service.validation.enabled:true}") boolean validationEnabled) {
        this.restTemplate = restTemplate;
        this.identityServiceUrl = identityServiceUrl;
        this.validationEnabled = validationEnabled;
    }
    
    /**
     * Validates that a technician exists and is active.
     * 
     * @param technicianId the ID of the technician to validate
     * @throws TechnicianNotFoundException if technician not found or inactive
     */
    public void validateTechnician(Long technicianId) {
        if (!validationEnabled) {
            log.debug("Technician validation is disabled, skipping validation for technician ID: {}", technicianId);
            return;
        }
        
        log.info("Validating technician with ID: {} against identity-svc at {}", technicianId, identityServiceUrl);
        
        try {
            String url = identityServiceUrl + "/api/users/" + technicianId;
            ResponseEntity<TechnicianInfo> response = restTemplate.getForEntity(url, TechnicianInfo.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                TechnicianInfo technicianInfo = response.getBody();
                
                // Check if user is active
                if (!technicianInfo.isActive()) {
                    log.warn("Technician {} is not active (status: {})", technicianId, technicianInfo.getStatus());
                    throw new TechnicianNotFoundException(technicianId, "is not active");
                }
                
                log.info("Technician {} validated successfully: {} ({})", 
                        technicianId, technicianInfo.getName(), technicianInfo.getStatus());
            } else {
                log.warn("Unexpected response for technician {}: {}", technicianId, response.getStatusCode());
                throw new TechnicianNotFoundException(technicianId);
            }
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Technician not found in identity-svc: {}", technicianId);
            throw new TechnicianNotFoundException(technicianId);
        } catch (RestClientException e) {
            log.error("Error calling identity-svc for technician {}: {}", technicianId, e.getMessage());
            // If identity-svc is unavailable, we log the error but allow the operation
            // This prevents task-svc from being blocked when identity-svc is down
            log.warn("Identity-svc unavailable, proceeding without technician validation for ID: {}", technicianId);
        }
    }
    
    /**
     * Gets technician information from identity-svc.
     * 
     * @param technicianId the ID of the technician
     * @return TechnicianInfo or null if not found or service unavailable
     */
    public TechnicianInfo getTechnicianInfo(Long technicianId) {
        if (!validationEnabled) {
            log.debug("Technician validation is disabled, returning null for technician ID: {}", technicianId);
            return null;
        }
        
        try {
            String url = identityServiceUrl + "/api/users/" + technicianId;
            ResponseEntity<TechnicianInfo> response = restTemplate.getForEntity(url, TechnicianInfo.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            log.warn("Error fetching technician info for ID {}: {}", technicianId, e.getMessage());
        }
        return null;
    }
}
