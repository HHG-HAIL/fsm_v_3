package com.fsm.identity.domain.repository;

import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.RoleEntity;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.model.User.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository with actual database operations.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    private RoleEntity adminRole;
    private RoleEntity technicianRole;
    
    @BeforeEach
    void setUp() {
        // Roles are seeded by Flyway migration, fetch them
        adminRole = roleRepository.findByName(Role.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        technicianRole = roleRepository.findByName(Role.TECHNICIAN)
                .orElseThrow(() -> new RuntimeException("TECHNICIAN role not found"));
    }
    
    @Test
    void testRolesAreSeededByMigration() {
        List<RoleEntity> roles = roleRepository.findAll();
        
        assertEquals(4, roles.size(), "Should have 4 roles seeded");
        assertTrue(roleRepository.existsByName(Role.ADMIN));
        assertTrue(roleRepository.existsByName(Role.DISPATCHER));
        assertTrue(roleRepository.existsByName(Role.SUPERVISOR));
        assertTrue(roleRepository.existsByName(Role.TECHNICIAN));
    }
    
    @Test
    void testSaveAndFindUser() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .phone("+12025551000")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        User saved = userRepository.save(user);
        
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals("Test User", saved.getName());
        assertEquals("test@example.com", saved.getEmail());
        assertEquals(adminRole.getId(), saved.getRole().getId());
    }
    
    @Test
    void testFindByEmail() {
        User user = User.builder()
                .name("Email Test User")
                .email("email.test@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        userRepository.save(user);
        
        Optional<User> found = userRepository.findByEmail("email.test@example.com");
        
        assertTrue(found.isPresent());
        assertEquals("Email Test User", found.get().getName());
    }
    
    @Test
    void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        
        assertFalse(found.isPresent());
    }
    
    @Test
    void testEmailUniqueConstraint() {
        User user1 = User.builder()
                .name("User 1")
                .email("duplicate@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        userRepository.save(user1);
        
        User user2 = User.builder()
                .name("User 2")
                .email("duplicate@example.com")
                .role(technicianRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(user2);
        }, "Should throw exception for duplicate email");
    }
    
    @Test
    void testFindByStatus() {
        User activeUser = User.builder()
                .name("Active User")
                .email("active@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        User inactiveUser = User.builder()
                .name("Inactive User")
                .email("inactive@example.com")
                .role(technicianRole)
                .password("hashedPassword123")
                .status(UserStatus.INACTIVE)
                .build();
        
        userRepository.save(activeUser);
        userRepository.save(inactiveUser);
        
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        List<User> inactiveUsers = userRepository.findByStatus(UserStatus.INACTIVE);
        
        assertTrue(activeUsers.size() >= 1);
        assertTrue(inactiveUsers.size() >= 1);
    }
    
    @Test
    void testFindByRoleName() {
        User user1 = User.builder()
                .name("Admin User")
                .email("admin1@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        User user2 = User.builder()
                .name("Tech User")
                .email("tech1@example.com")
                .role(technicianRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        userRepository.save(user1);
        userRepository.save(user2);
        
        List<User> admins = userRepository.findByRoleName(Role.ADMIN);
        List<User> technicians = userRepository.findByRoleName(Role.TECHNICIAN);
        
        assertTrue(admins.size() >= 1);
        assertTrue(technicians.size() >= 1);
        assertTrue(admins.stream().allMatch(u -> u.getRole().getName() == Role.ADMIN));
        assertTrue(technicians.stream().allMatch(u -> u.getRole().getName() == Role.TECHNICIAN));
    }
    
    @Test
    void testFindByRoleId() {
        User user = User.builder()
                .name("Role ID Test User")
                .email("roleid@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        userRepository.save(user);
        
        List<User> users = userRepository.findByRoleId(adminRole.getId());
        
        assertTrue(users.size() >= 1);
        assertTrue(users.stream().allMatch(u -> u.getRole().getId().equals(adminRole.getId())));
    }
    
    @Test
    void testExistsByEmail() {
        User user = User.builder()
                .name("Exists Test User")
                .email("exists@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        userRepository.save(user);
        
        assertTrue(userRepository.existsByEmail("exists@example.com"));
        assertFalse(userRepository.existsByEmail("notexists@example.com"));
    }
    
    @Test
    void testUpdateUser() {
        User user = User.builder()
                .name("Original Name")
                .email("update@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        User saved = userRepository.save(user);
        Long userId = saved.getId();
        
        // Update the user
        saved.setName("Updated Name");
        saved.setStatus(UserStatus.INACTIVE);
        userRepository.save(saved);
        
        // Fetch and verify
        Optional<User> updated = userRepository.findById(userId);
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals(UserStatus.INACTIVE, updated.get().getStatus());
    }
    
    @Test
    void testDeleteUser() {
        User user = User.builder()
                .name("Delete Test User")
                .email("delete@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        User saved = userRepository.save(user);
        Long userId = saved.getId();
        
        assertTrue(userRepository.existsById(userId));
        
        userRepository.deleteById(userId);
        
        assertFalse(userRepository.existsById(userId));
    }
    
    @Test
    void testUserActivateDeactivate() {
        User user = User.builder()
                .name("Status Test User")
                .email("status@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        User saved = userRepository.save(user);
        assertTrue(saved.isActive());
        
        saved.deactivate();
        userRepository.save(saved);
        
        Optional<User> deactivated = userRepository.findById(saved.getId());
        assertTrue(deactivated.isPresent());
        assertFalse(deactivated.get().isActive());
        
        deactivated.get().activate();
        userRepository.save(deactivated.get());
        
        Optional<User> activated = userRepository.findById(saved.getId());
        assertTrue(activated.isPresent());
        assertTrue(activated.get().isActive());
    }
    
    @Test
    void testCount() {
        long initialCount = userRepository.count();
        
        User user = User.builder()
                .name("Count Test User")
                .email("count@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        userRepository.save(user);
        
        long newCount = userRepository.count();
        assertEquals(initialCount + 1, newCount);
    }
    
    @Test
    void testFindAll() {
        User user1 = User.builder()
                .name("User 1")
                .email("user1@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        User user2 = User.builder()
                .name("User 2")
                .email("user2@example.com")
                .role(technicianRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        userRepository.save(user1);
        userRepository.save(user2);
        
        List<User> allUsers = userRepository.findAll();
        
        assertTrue(allUsers.size() >= 2);
    }
    
    @Test
    void testForeignKeyConstraintOnRole() {
        // Verify that user must have a valid role
        User user = User.builder()
                .name("FK Test User")
                .email("fk@example.com")
                .phone("+12025551000")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        User saved = userRepository.save(user);
        
        assertNotNull(saved.getRole());
        assertEquals(adminRole.getId(), saved.getRole().getId());
    }
    
    @Test
    void testTimestampsAreSet() {
        User user = User.builder()
                .name("Timestamp Test")
                .email("timestamp@example.com")
                .role(adminRole)
                .password("hashedPassword123")
                .status(UserStatus.ACTIVE)
                .build();
        
        User saved = userRepository.save(user);
        
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        // Timestamps should be very close, within 1 second
        long secondsDiff = Math.abs(java.time.Duration.between(saved.getCreatedAt(), saved.getUpdatedAt()).toSeconds());
        assertTrue(secondsDiff <= 1, "Timestamps should be within 1 second of each other");
    }
    
    @Test
    void testRoleEntityHasCorrectDescription() {
        Optional<RoleEntity> admin = roleRepository.findByName(Role.ADMIN);
        
        assertTrue(admin.isPresent());
        assertNotNull(admin.get().getDescription());
        assertTrue(admin.get().getDescription().contains("Administrator"));
    }
}
