package com.hospital.repository;

import com.hospital.entity.Role;
import com.hospital.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repository for role catalog. */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(RoleName roleName);
}
