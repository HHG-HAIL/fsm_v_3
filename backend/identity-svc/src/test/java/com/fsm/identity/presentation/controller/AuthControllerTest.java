package com.fsm.identity.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.identity.application.dto.LoginRequest;
import com.fsm.identity.application.dto.LoginResponse;
import com.fsm.identity.application.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 */
@WebMvcTest(AuthController.class)
@ContextConfiguration(classes = {AuthController.class, AuthControllerTest.TestSecurityConfig.class})
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
    
    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .mobile(false)
                .build();
        
        LoginResponse response = LoginResponse.builder()
                .token("mock-jwt-token")
                .userId(1L)
                .name("Test User")
                .email("test@example.com")
                .role("ADMIN")
                .build();
        
        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
    
    @Test
    void testLoginInvalidCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();
        
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
    
    @Test
    void testLoginWithInvalidEmail() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("invalid-email")
                .password("password123")
                .build();
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testLoginWithBlankEmail() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("")
                .password("password123")
                .build();
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testLoginWithBlankPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("")
                .build();
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testLoginWithMissingEmail() throws Exception {
        String json = "{\"password\":\"password123\"}";
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testLoginWithMissingPassword() throws Exception {
        String json = "{\"email\":\"test@example.com\"}";
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testLoginWithMobileFlag() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .mobile(true)
                .build();
        
        LoginResponse response = LoginResponse.builder()
                .token("mock-jwt-token-mobile")
                .userId(1L)
                .name("Test User")
                .email("test@example.com")
                .role("TECHNICIAN")
                .build();
        
        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token-mobile"));
    }
    
    @Test
    void testLoginInactiveUser() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
        
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("User account is inactive"));
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User account is inactive"));
    }
}
