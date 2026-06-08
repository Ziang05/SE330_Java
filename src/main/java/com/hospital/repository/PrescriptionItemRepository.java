package com.hospital.repository;

import com.hospital.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for prescription line items.
 */
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    /**
     * Lấy tất cả dòng thuốc theo medical_record_id (qua Prescription).
     */
    @Query("SELECT pi FROM PrescriptionItem pi " +
            "JOIN pi.prescription p " +
            "WHERE p.medicalRecord.id = :medicalRecordId")
    List<PrescriptionItem> findByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);

    /**
     * Tính tổng tiền thuốc trong một medical record: SUM(quantity * unitPriceAtTime).
     */
    @Query("SELECT COALESCE(SUM(pi.quantity * pi.unitPriceAtTime), 0) " +
            "FROM PrescriptionItem pi " +
            "JOIN pi.prescription p " +
            "WHERE p.medicalRecord.id = :medicalRecordId")
    BigDecimal sumMedicineFeByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);
}
