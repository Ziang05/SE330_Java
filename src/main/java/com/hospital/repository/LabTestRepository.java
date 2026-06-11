package com.hospital.repository;

import com.hospital.entity.LabTest;
import com.hospital.entity.enums.LabTestStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for lab tests.
 */
public interface LabTestRepository extends JpaRepository<LabTest, Long> {

    /**
     * Lấy tất cả xét nghiệm theo medical_record_id.
     */
    List<LabTest> findByMedicalRecordId(Long medicalRecordId);

    /**
     * Tính tổng phí xét nghiệm của một medical record.
     */
    @Query("SELECT COALESCE(SUM(lt.fee), 0) FROM LabTest lt WHERE lt.medicalRecord.id = :medicalRecordId")
    BigDecimal sumLabFeeByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);

    List<LabTest> findByStatus(LabTestStatus status);
}
