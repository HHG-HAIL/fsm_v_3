package com.fsm.identity.application.service;

import com.fsm.identity.application.dto.LoginRequest;
import com.fsm.identity.application.dto.LoginResponse;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.repository.UserRepository;
import com.fsm.identity.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service handling user login and JWT token generation.
 * Domain invariants:
 * - Passwords are never returned in responses
 * - Failed login attempts are logged
 * - Only active users can log in
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * Authenticate user and generate JWT token
     * 
     * @param loginRequest Login credentials
     * @return LoginResponse with JWT token and user info
     * @throws BadCredentialsException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", loginRequest.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });
        
        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for user: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
        
        // Check if user is active
        if (!user.isActive()) {
            log.warn("Login failed - user is inactive: {}", loginRequest.getEmail());
            throw new BadCredentialsException("User account is inactive");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().getName(),
                loginRequest.isMobile()
        );
        
        log.info("Login successful for user: {}", loginRequest.getEmail());
        
        // Return response (without password)
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .build();
    }
}
