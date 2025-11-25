package com.fsm.task.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for technician validation response from identity-svc.
 * Used to check if a technician exists and is active.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianInfo {
    
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    
    /**
     * Check if the technician is active
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
    
    /**
     * Check if the user has technician role
     * @return true if role is TECHNICIAN
     */
    public boolean isTechnician() {
        return "TECHNICIAN".equalsIgnoreCase(role);
    }
}
