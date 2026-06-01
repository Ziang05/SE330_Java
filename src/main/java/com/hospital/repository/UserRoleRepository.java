package com.hospital.repository;

import com.hospital.entity.UserRole;
import com.hospital.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Repository for user-role assignments. */
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("select ur from UserRole ur join fetch ur.role where ur.user.id = :userId")
    List<UserRole> findByUserIdWithRole(@Param("userId") Long userId);

    @Modifying
    @Query("delete from UserRole ur where ur.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
