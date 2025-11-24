package com.fsm.identity.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Role enum
 */
class RoleTest {
    
    @Test
    void testAllRolesExist() {
        Role[] roles = Role.values();
        assertEquals(4, roles.length, "Should have exactly 4 roles");
    }
    
    @Test
    void testAdminRole() {
        Role admin = Role.ADMIN;
        assertNotNull(admin);
        assertEquals("ADMIN", admin.name());
        assertTrue(admin.getDescription().contains("Administrator"));
    }
    
    @Test
    void testDispatcherRole() {
        Role dispatcher = Role.DISPATCHER;
        assertNotNull(dispatcher);
        assertEquals("DISPATCHER", dispatcher.name());
        assertTrue(dispatcher.getDescription().contains("Dispatcher"));
    }
    
    @Test
    void testSupervisorRole() {
        Role supervisor = Role.SUPERVISOR;
        assertNotNull(supervisor);
        assertEquals("SUPERVISOR", supervisor.name());
        assertTrue(supervisor.getDescription().contains("Supervisor"));
    }
    
    @Test
    void testTechnicianRole() {
        Role technician = Role.TECHNICIAN;
        assertNotNull(technician);
        assertEquals("TECHNICIAN", technician.name());
        assertTrue(technician.getDescription().contains("Technician"));
    }
    
    @Test
    void testRoleValueOf() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.DISPATCHER, Role.valueOf("DISPATCHER"));
        assertEquals(Role.SUPERVISOR, Role.valueOf("SUPERVISOR"));
        assertEquals(Role.TECHNICIAN, Role.valueOf("TECHNICIAN"));
    }
    
    @Test
    void testInvalidRoleValueOf() {
        assertThrows(IllegalArgumentException.class, () -> {
            Role.valueOf("INVALID_ROLE");
        });
    }
    
    @Test
    void testRoleDescriptions() {
        assertNotNull(Role.ADMIN.getDescription());
        assertNotNull(Role.DISPATCHER.getDescription());
        assertNotNull(Role.SUPERVISOR.getDescription());
        assertNotNull(Role.TECHNICIAN.getDescription());
        
        assertFalse(Role.ADMIN.getDescription().isEmpty());
        assertFalse(Role.DISPATCHER.getDescription().isEmpty());
        assertFalse(Role.SUPERVISOR.getDescription().isEmpty());
        assertFalse(Role.TECHNICIAN.getDescription().isEmpty());
    }
}
