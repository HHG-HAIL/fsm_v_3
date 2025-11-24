package com.fsm.identity.domain.repository;

import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for RoleEntity.
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    
    /**
     * Find role by name
     */
    Optional<RoleEntity> findByName(Role name);
    
    /**
     * Check if role exists by name
     */
    boolean existsByName(Role name);
}
