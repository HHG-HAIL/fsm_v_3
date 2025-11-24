package com.fsm.identity.application.dto;

import com.fsm.identity.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response DTO.
 * Contains JWT token and user information.
 * Domain invariant: Never includes password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String token;
    private Long userId;
    private String name;
    private String email;
    private String role;
}
