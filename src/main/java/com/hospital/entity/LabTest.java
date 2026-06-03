package com.hospital.entity;

import com.hospital.entity.enums.LabTestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lab test order and result associated with a medical record.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lab_tests")
public class LabTest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Column(name = "test_type", nullable = false, length = 120)
    private String testType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_by", nullable = false)
    private Doctor orderedBy;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "result_file_url", length = 255)
    private String resultFileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LabTestStatus status = LabTestStatus.ORDERED;

    @Column(name = "test_date")
    private LocalDateTime testDate;

    /**
     * Phí xét nghiệm (VNĐ) – dùng để tính labFee trên Invoice.
     */
    @Column(name = "fee", precision = 12, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;
}
