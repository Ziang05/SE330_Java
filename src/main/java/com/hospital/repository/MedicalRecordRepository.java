package com.hospital.repository;

import com.hospital.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for medical records.
 */
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
}
