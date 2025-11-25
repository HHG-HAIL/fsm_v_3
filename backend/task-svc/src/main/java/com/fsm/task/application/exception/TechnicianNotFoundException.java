package com.fsm.task.application.exception;

/**
 * Exception thrown when a technician is not found or is inactive.
 */
public class TechnicianNotFoundException extends RuntimeException {
    
    private final Long technicianId;
    
    public TechnicianNotFoundException(Long technicianId) {
        super("Technician not found with ID: " + technicianId);
        this.technicianId = technicianId;
    }
    
    public TechnicianNotFoundException(Long technicianId, String reason) {
        super("Technician with ID " + technicianId + " " + reason);
        this.technicianId = technicianId;
    }
    
    public Long getTechnicianId() {
        return technicianId;
    }
}
