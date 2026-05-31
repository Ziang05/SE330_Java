package com.hospital.repository;

import com.hospital.entity.UserRole;
import com.hospital.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for user-role assignments. */
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
