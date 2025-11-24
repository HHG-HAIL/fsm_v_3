package com.fsm.identity.domain.model;

/**
 * Role enum representing user roles in the FSM system.
 * Each user must have exactly one role.
 */
public enum Role {
    ADMIN("Administrator - Full system access"),
    DISPATCHER("Dispatcher - Manages task creation and assignment"),
    SUPERVISOR("Supervisor - Monitors operations and analyzes performance"),
    TECHNICIAN("Technician - Completes field tasks");
    
    private final String description;
    
    Role(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
