package com.fsm.identity.presentation.controller;

import com.fsm.identity.application.dto.LoginRequest;
import com.fsm.identity.application.dto.LoginResponse;
import com.fsm.identity.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller handling login and authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Login endpoint for user authentication
     * 
     * @param loginRequest Login credentials
     * @return LoginResponse with JWT token and user info
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content)
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Login failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Error response DTO for consistent error handling
     */
    private record ErrorResponse(String message) {
    }
}
