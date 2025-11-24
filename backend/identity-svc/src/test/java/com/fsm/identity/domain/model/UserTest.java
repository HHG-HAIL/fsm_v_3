package com.fsm.identity.domain.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 */
class UserTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testUserBuilderCreatesValidUser() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+12025551234")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("+12025551234", user.getPhone());
        assertEquals(Role.TECHNICIAN, user.getRole());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
    }
    
    @Test
    void testUserDefaultStatus() {
        User user = User.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .role(Role.DISPATCHER)
                .build();
        
        assertEquals(User.UserStatus.ACTIVE, user.getStatus(), 
                "Default status should be ACTIVE");
    }
    
    @Test
    void testUserActivateMethod() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.INACTIVE)
                .build();
        
        assertFalse(user.isActive());
        user.activate();
        assertTrue(user.isActive());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
    }
    
    @Test
    void testUserDeactivateMethod() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        assertTrue(user.isActive());
        user.deactivate();
        assertFalse(user.isActive());
        assertEquals(User.UserStatus.INACTIVE, user.getStatus());
    }
    
    @Test
    void testUserIsActiveMethod() {
        User activeUser = User.builder()
                .name("Active User")
                .email("active@example.com")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        User inactiveUser = User.builder()
                .name("Inactive User")
                .email("inactive@example.com")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.INACTIVE)
                .build();
        
        assertTrue(activeUser.isActive());
        assertFalse(inactiveUser.isActive());
    }
    
    @Test
    void testValidationWithBlankName() {
        User user = User.builder()
                .name("")
                .email("test@example.com")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
    
    @Test
    void testValidationWithNullName() {
        User user = User.builder()
                .name(null)
                .email("test@example.com")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
    
    @Test
    void testValidationWithInvalidEmail() {
        User user = User.builder()
                .name("Test User")
                .email("invalid-email")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
    
    @Test
    void testValidationWithBlankEmail() {
        User user = User.builder()
                .name("Test User")
                .email("")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
    
    @Test
    void testValidationWithNullRole() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .role(null)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }
    
    @Test
    void testValidationWithNullStatus() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .role(Role.TECHNICIAN)
                .status(null)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }
    
    @Test
    void testValidationWithInvalidPhoneFormat() {
        String[] invalidPhones = {
            "abc123",        // Contains letters
            "+",             // Just a plus sign
            "0123456789"     // Starts with 0 without +
        };
        
        for (String phone : invalidPhones) {
            User user = User.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .phone(phone)
                    .role(Role.TECHNICIAN)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), 
                    "Phone " + phone + " should be invalid");
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("phone")),
                    "Phone " + phone + " should have validation error");
        }
    }
    
    @Test
    void testValidationWithValidPhoneFormats() {
        String[] validPhones = {
            "+12025551234",
            "+442071234567",
            "+919876543210"
        };
        
        for (String phone : validPhones) {
            User user = User.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .phone(phone)
                    .role(Role.TECHNICIAN)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertTrue(violations.isEmpty() || violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("phone")),
                    "Phone " + phone + " should be valid");
        }
    }
    
    @Test
    void testValidationWithNullPhone() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .phone(null)
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        // Phone is optional, so null should be valid
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }
    
    @Test
    void testUserWithAllRoles() {
        Role[] roles = Role.values();
        
        for (Role role : roles) {
            User user = User.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .role(role)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            
            assertEquals(role, user.getRole());
        }
    }
    
    @Test
    void testUserStatusEnum() {
        assertEquals(2, User.UserStatus.values().length, 
                "Should have exactly 2 status values");
        assertNotNull(User.UserStatus.ACTIVE);
        assertNotNull(User.UserStatus.INACTIVE);
    }
    
    @Test
    void testUserEqualsAndHashCode() {
        User user1 = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        User user2 = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
    
    @Test
    void testUserToString() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.TECHNICIAN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        String toString = user.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("John Doe"));
        assertTrue(toString.contains("john@example.com"));
    }
}
