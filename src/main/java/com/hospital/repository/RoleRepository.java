package com.hospital.repository;

import com.hospital.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for role catalog. */
public interface RoleRepository extends JpaRepository<Role, Long> {
}
