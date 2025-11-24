package com.fsm.identity.domain.repository;

import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.RoleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RoleRepository with actual database operations.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class RoleRepositoryTest {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Test
    void testRolesAreSeededByMigration() {
        List<RoleEntity> roles = roleRepository.findAll();
        
        assertEquals(4, roles.size(), "Should have 4 roles seeded by migration");
    }
    
    @Test
    void testFindByNameAdmin() {
        Optional<RoleEntity> admin = roleRepository.findByName(Role.ADMIN);
        
        assertTrue(admin.isPresent());
        assertEquals(Role.ADMIN, admin.get().getName());
        assertNotNull(admin.get().getDescription());
        assertTrue(admin.get().getDescription().contains("Administrator"));
    }
    
    @Test
    void testFindByNameDispatcher() {
        Optional<RoleEntity> dispatcher = roleRepository.findByName(Role.DISPATCHER);
        
        assertTrue(dispatcher.isPresent());
        assertEquals(Role.DISPATCHER, dispatcher.get().getName());
        assertNotNull(dispatcher.get().getDescription());
        assertTrue(dispatcher.get().getDescription().contains("Dispatcher"));
    }
    
    @Test
    void testFindByNameSupervisor() {
        Optional<RoleEntity> supervisor = roleRepository.findByName(Role.SUPERVISOR);
        
        assertTrue(supervisor.isPresent());
        assertEquals(Role.SUPERVISOR, supervisor.get().getName());
        assertNotNull(supervisor.get().getDescription());
        assertTrue(supervisor.get().getDescription().contains("Supervisor"));
    }
    
    @Test
    void testFindByNameTechnician() {
        Optional<RoleEntity> technician = roleRepository.findByName(Role.TECHNICIAN);
        
        assertTrue(technician.isPresent());
        assertEquals(Role.TECHNICIAN, technician.get().getName());
        assertNotNull(technician.get().getDescription());
        assertTrue(technician.get().getDescription().contains("Technician"));
    }
    
    @Test
    void testExistsByName() {
        assertTrue(roleRepository.existsByName(Role.ADMIN));
        assertTrue(roleRepository.existsByName(Role.DISPATCHER));
        assertTrue(roleRepository.existsByName(Role.SUPERVISOR));
        assertTrue(roleRepository.existsByName(Role.TECHNICIAN));
    }
    
    @Test
    void testAllRolesHaveIds() {
        List<RoleEntity> roles = roleRepository.findAll();
        
        for (RoleEntity role : roles) {
            assertNotNull(role.getId());
            assertTrue(role.getId() > 0);
        }
    }
    
    @Test
    void testAllRolesHaveTimestamps() {
        List<RoleEntity> roles = roleRepository.findAll();
        
        for (RoleEntity role : roles) {
            assertNotNull(role.getCreatedAt());
            assertNotNull(role.getUpdatedAt());
        }
    }
    
    @Test
    void testRoleNamesAreUnique() {
        List<RoleEntity> roles = roleRepository.findAll();
        
        long uniqueNames = roles.stream()
                .map(RoleEntity::getName)
                .distinct()
                .count();
        
        assertEquals(roles.size(), uniqueNames, "All role names should be unique");
    }
}
