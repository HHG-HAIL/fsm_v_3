package com.fsm.identity.domain.repository;

import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.model.User.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRepository
 */
class UserRepositoryTest {
    
    private UserRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }
    
    @Test
    void testRepositoryInitializesWithHardcodedUsers() {
        List<User> users = repository.findAll();
        
        assertNotNull(users);
        assertEquals(4, users.size(), "Should have 4 hardcoded users");
    }
    
    @Test
    void testHardcodedUsersHaveDifferentRoles() {
        List<User> users = repository.findAll();
        
        boolean hasAdmin = users.stream().anyMatch(u -> u.getRole() == Role.ADMIN);
        boolean hasDispatcher = users.stream().anyMatch(u -> u.getRole() == Role.DISPATCHER);
        boolean hasSupervisor = users.stream().anyMatch(u -> u.getRole() == Role.SUPERVISOR);
        boolean hasTechnician = users.stream().anyMatch(u -> u.getRole() == Role.TECHNICIAN);
        
        assertTrue(hasAdmin, "Should have an ADMIN user");
        assertTrue(hasDispatcher, "Should have a DISPATCHER user");
        assertTrue(hasSupervisor, "Should have a SUPERVISOR user");
        assertTrue(hasTechnician, "Should have a TECHNICIAN user");
    }
    
    @Test
    void testHardcodedUsersAreActive() {
        List<User> users = repository.findAll();
        
        assertTrue(users.stream().allMatch(User::isActive), 
                "All hardcoded users should be active");
    }
    
    @Test
    void testHardcodedUsersHaveUniqueEmails() {
        List<User> users = repository.findAll();
        
        long uniqueEmailCount = users.stream()
                .map(User::getEmail)
                .distinct()
                .count();
        
        assertEquals(users.size(), uniqueEmailCount, 
                "All emails should be unique");
    }
    
    @Test
    void testFindById() {
        List<User> users = repository.findAll();
        User firstUser = users.get(0);
        
        Optional<User> found = repository.findById(firstUser.getId());
        
        assertTrue(found.isPresent());
        assertEquals(firstUser.getId(), found.get().getId());
        assertEquals(firstUser.getEmail(), found.get().getEmail());
    }
    
    @Test
    void testFindByIdNotFound() {
        Optional<User> found = repository.findById(999L);
        
        assertFalse(found.isPresent());
    }
    
    @Test
    void testFindByEmail() {
        Optional<User> found = repository.findByEmail("admin@fsm.com");
        
        assertTrue(found.isPresent());
        assertEquals("admin@fsm.com", found.get().getEmail());
        assertEquals(Role.ADMIN, found.get().getRole());
    }
    
    @Test
    void testFindByEmailNotFound() {
        Optional<User> found = repository.findByEmail("nonexistent@example.com");
        
        assertFalse(found.isPresent());
    }
    
    @Test
    void testFindByRole() {
        List<User> admins = repository.findByRole(Role.ADMIN);
        
        assertNotNull(admins);
        assertFalse(admins.isEmpty());
        assertTrue(admins.stream().allMatch(u -> u.getRole() == Role.ADMIN));
    }
    
    @Test
    void testFindByRoleTechnician() {
        List<User> technicians = repository.findByRole(Role.TECHNICIAN);
        
        assertNotNull(technicians);
        assertFalse(technicians.isEmpty());
        assertTrue(technicians.stream().allMatch(u -> u.getRole() == Role.TECHNICIAN));
    }
    
    @Test
    void testFindByStatus() {
        List<User> activeUsers = repository.findByStatus(UserStatus.ACTIVE);
        
        assertNotNull(activeUsers);
        assertEquals(4, activeUsers.size(), "All hardcoded users should be active");
        assertTrue(activeUsers.stream().allMatch(User::isActive));
    }
    
    @Test
    void testFindByStatusInactive() {
        List<User> inactiveUsers = repository.findByStatus(UserStatus.INACTIVE);
        
        assertNotNull(inactiveUsers);
        assertTrue(inactiveUsers.isEmpty(), "No hardcoded users should be inactive");
    }
    
    @Test
    void testSaveNewUser() {
        User newUser = User.builder()
                .name("New User")
                .email("newuser@example.com")
                .phone("+12025559999")
                .role(Role.TECHNICIAN)
                .status(UserStatus.ACTIVE)
                .build();
        
        User saved = repository.save(newUser);
        
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("newuser@example.com", saved.getEmail());
        
        // Verify it's in the repository
        Optional<User> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }
    
    @Test
    void testSaveNewUserWithDuplicateEmail() {
        User newUser = User.builder()
                .name("Duplicate Email User")
                .email("admin@fsm.com")  // Existing email
                .role(Role.TECHNICIAN)
                .status(UserStatus.ACTIVE)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            repository.save(newUser);
        }, "Should throw exception for duplicate email");
    }
    
    @Test
    void testUpdateExistingUser() {
        List<User> users = repository.findAll();
        User existingUser = users.get(0);
        
        // Update the user
        existingUser.setName("Updated Name");
        User updated = repository.save(existingUser);
        
        assertEquals("Updated Name", updated.getName());
        
        // Verify the update persisted
        Optional<User> found = repository.findById(existingUser.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
    }
    
    @Test
    void testUpdateUserEmailToExistingEmail() {
        List<User> users = repository.findAll();
        User user1 = users.get(0);
        User user2 = users.get(1);
        String user2Email = user2.getEmail(); // Store before modification
        
        // Try to update user1's email to user2's email
        user1.setEmail(user2Email);
        
        assertThrows(IllegalArgumentException.class, () -> {
            repository.save(user1);
        }, "Should throw exception when updating to duplicate email");
    }
    
    @Test
    void testUpdateUserEmailToNewEmail() {
        List<User> users = repository.findAll();
        User existingUser = users.get(0);
        String oldEmail = existingUser.getEmail();
        String newEmail = "newemail@example.com";
        
        existingUser.setEmail(newEmail);
        User updated = repository.save(existingUser);
        
        assertEquals(newEmail, updated.getEmail());
        
        // Old email should not exist
        assertFalse(repository.findByEmail(oldEmail).isPresent());
        
        // New email should exist
        assertTrue(repository.findByEmail(newEmail).isPresent());
    }
    
    @Test
    void testDeleteById() {
        List<User> users = repository.findAll();
        User userToDelete = users.get(0);
        Long idToDelete = userToDelete.getId();
        
        assertTrue(repository.existsById(idToDelete));
        
        repository.deleteById(idToDelete);
        
        assertFalse(repository.existsById(idToDelete));
        assertFalse(repository.findById(idToDelete).isPresent());
    }
    
    @Test
    void testExistsById() {
        List<User> users = repository.findAll();
        User existingUser = users.get(0);
        
        assertTrue(repository.existsById(existingUser.getId()));
        assertFalse(repository.existsById(999L));
    }
    
    @Test
    void testExistsByEmail() {
        assertTrue(repository.existsByEmail("admin@fsm.com"));
        assertFalse(repository.existsByEmail("nonexistent@example.com"));
    }
    
    @Test
    void testCount() {
        long count = repository.count();
        assertEquals(4, count, "Should have 4 hardcoded users");
    }
    
    @Test
    void testCountAfterAddingUser() {
        long initialCount = repository.count();
        
        User newUser = User.builder()
                .name("New User")
                .email("newuser@example.com")
                .role(Role.TECHNICIAN)
                .status(UserStatus.ACTIVE)
                .build();
        
        repository.save(newUser);
        
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
    
    @Test
    void testCountAfterDeletingUser() {
        long initialCount = repository.count();
        
        List<User> users = repository.findAll();
        User userToDelete = users.get(0);
        
        repository.deleteById(userToDelete.getId());
        
        long newCount = repository.count();
        assertEquals(initialCount - 1, newCount);
    }
    
    @Test
    void testSaveUserWithInactiveStatus() {
        User inactiveUser = User.builder()
                .name("Inactive User")
                .email("inactive@example.com")
                .role(Role.TECHNICIAN)
                .status(UserStatus.INACTIVE)
                .build();
        
        User saved = repository.save(inactiveUser);
        
        assertNotNull(saved);
        assertFalse(saved.isActive());
        assertEquals(UserStatus.INACTIVE, saved.getStatus());
        
        // Verify it can be found by status
        List<User> inactiveUsers = repository.findByStatus(UserStatus.INACTIVE);
        assertTrue(inactiveUsers.stream().anyMatch(u -> u.getId().equals(saved.getId())));
    }
    
    @Test
    void testActivateAndDeactivateUser() {
        List<User> users = repository.findAll();
        User user = users.get(0);
        
        // Deactivate
        user.deactivate();
        repository.save(user);
        
        Optional<User> found = repository.findById(user.getId());
        assertTrue(found.isPresent());
        assertFalse(found.get().isActive());
        
        // Activate
        user.activate();
        repository.save(user);
        
        found = repository.findById(user.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isActive());
    }
    
    @Test
    void testHardcodedAdminUser() {
        Optional<User> admin = repository.findByEmail("admin@fsm.com");
        
        assertTrue(admin.isPresent());
        assertEquals("John Administrator", admin.get().getName());
        assertEquals(Role.ADMIN, admin.get().getRole());
        assertTrue(admin.get().isActive());
        assertNotNull(admin.get().getPhone());
    }
    
    @Test
    void testHardcodedDispatcherUser() {
        Optional<User> dispatcher = repository.findByEmail("sarah.dispatcher@fsm.com");
        
        assertTrue(dispatcher.isPresent());
        assertEquals("Sarah Dispatcher", dispatcher.get().getName());
        assertEquals(Role.DISPATCHER, dispatcher.get().getRole());
        assertTrue(dispatcher.get().isActive());
    }
    
    @Test
    void testHardcodedSupervisorUser() {
        Optional<User> supervisor = repository.findByEmail("mike.supervisor@fsm.com");
        
        assertTrue(supervisor.isPresent());
        assertEquals("Mike Supervisor", supervisor.get().getName());
        assertEquals(Role.SUPERVISOR, supervisor.get().getRole());
        assertTrue(supervisor.get().isActive());
    }
    
    @Test
    void testHardcodedTechnicianUser() {
        Optional<User> technician = repository.findByEmail("tom.technician@fsm.com");
        
        assertTrue(technician.isPresent());
        assertEquals("Tom Technician", technician.get().getName());
        assertEquals(Role.TECHNICIAN, technician.get().getRole());
        assertTrue(technician.get().isActive());
    }
}
