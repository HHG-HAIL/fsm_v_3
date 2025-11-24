package com.fsm.identity.domain.repository;

import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.model.User.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for User entity.
 * Provides database persistence operations for users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * Find users by role ID
     */
    @Query("SELECT u FROM User u WHERE u.role.id = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") Role roleName);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}
