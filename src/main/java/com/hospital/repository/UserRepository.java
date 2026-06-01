package com.hospital.repository;

import com.hospital.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Repository for users; security integration will be added later. */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            select u from User u
            where (:isActive is null or u.isActive = :isActive)
              and (
                    :keyword is null
                    or lower(u.username) like lower(concat('%', :keyword, '%'))
                    or lower(u.email) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<User> findByKeywordAndStatus(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
