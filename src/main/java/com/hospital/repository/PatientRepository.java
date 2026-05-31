package com.hospital.repository;

import com.hospital.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repository for patient lookup and persistence. */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByCccd(String cccd);

    Optional<Patient> findByPhone(String phone);

    List<Patient> findByFullNameContainingIgnoreCase(String fullName);
}
