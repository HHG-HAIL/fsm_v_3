package com.fsm.identity.application.service;

import com.fsm.identity.application.dto.LoginRequest;
import com.fsm.identity.application.dto.LoginResponse;
import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.RoleEntity;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.repository.UserRepository;
import com.fsm.identity.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private RoleEntity adminRole;
    
    @BeforeEach
    void setUp() {
        adminRole = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .description("Admin role")
                .build();
        
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("$2a$10$hashedPassword")
                .role(adminRole)
                .status(User.UserStatus.ACTIVE)
                .build();
    }
    
    @Test
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .mobile(false)
                .build();
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false))
                .thenReturn("mock-jwt-token");
        
        LoginResponse response = authService.login(request);
        
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtUtil).generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false);
    }
    
    @Test
    void testLoginWithMobileFlag() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .mobile(true)
                .build();
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, true))
                .thenReturn("mock-jwt-token-mobile");
        
        LoginResponse response = authService.login(request);
        
        assertNotNull(response);
        assertEquals("mock-jwt-token-mobile", response.getToken());
        
        verify(jwtUtil).generateToken(1L, "test@example.com", "Test User", Role.ADMIN, true);
    }
    
    @Test
    void testLoginUserNotFound() {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();
        
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, 
                () -> authService.login(request));
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyString(), any(), anyBoolean());
    }
    
    @Test
    void testLoginInvalidPassword() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedPassword")).thenReturn(false);
        
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, 
                () -> authService.login(request));
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("wrongpassword", "$2a$10$hashedPassword");
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyString(), any(), anyBoolean());
    }
    
    @Test
    void testLoginInactiveUser() {
        testUser.deactivate(); // Set user status to INACTIVE
        
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, 
                () -> authService.login(request));
        
        assertEquals("User account is inactive", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyString(), any(), anyBoolean());
    }
    
    @Test
    void testLoginWithDifferentRoles() {
        Role[] roles = {Role.ADMIN, Role.DISPATCHER, Role.SUPERVISOR, Role.TECHNICIAN};
        
        for (Role role : roles) {
            RoleEntity roleEntity = RoleEntity.builder()
                    .id(1L)
                    .name(role)
                    .description(role.getDescription())
                    .build();
            
            User user = User.builder()
                    .id(1L)
                    .name("Test User")
                    .email("test@example.com")
                    .password("$2a$10$hashedPassword")
                    .role(roleEntity)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();
            
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
            when(jwtUtil.generateToken(1L, "test@example.com", "Test User", role, false))
                    .thenReturn("mock-jwt-token");
            
            LoginResponse response = authService.login(request);
            
            assertEquals(role.name(), response.getRole());
        }
    }
    
    @Test
    void testLoginDoesNotReturnPassword() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString(), any(), anyBoolean()))
                .thenReturn("mock-jwt-token");
        
        LoginResponse response = authService.login(request);
        
        // Verify response class doesn't have password field by checking toString
        assertFalse(response.toString().contains("password"));
        assertFalse(response.toString().contains("$2a$10$hashedPassword"));
    }
}
