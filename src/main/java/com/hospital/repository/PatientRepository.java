package com.hospital.repository;

import com.hospital.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for patient lookup and persistence.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByCccd(String cccd);

    Optional<Patient> findByPhone(String phone);

    List<Patient> findByFullNameContainingIgnoreCase(String fullName);

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.cccd) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.insuranceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Patient> searchByKeyword(@Param("keyword") String keyword);
}
