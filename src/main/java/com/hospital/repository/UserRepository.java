package com.hospital.repository;

import com.hospital.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repository for users; security integration will be added later. */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
