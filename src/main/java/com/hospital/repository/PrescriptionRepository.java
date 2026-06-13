package com.hospital.repository;

import com.hospital.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for prescriptions.
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    java.util.List<Prescription> findByMedicalRecordPatientIdOrderByCreatedAtDesc(Long patientId);
}
