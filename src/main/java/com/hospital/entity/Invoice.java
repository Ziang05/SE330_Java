package com.hospital.entity;

import com.hospital.entity.enums.InsuranceCoverage;
import com.hospital.entity.enums.InvoiceStatus;
import com.hospital.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Billing invoice for a medical record visit.
 *
 * <p>Columns:
 * <ul>
 *   <li>examination_fee  – phí khám bác sĩ</li>
 *   <li>medicine_fee     – tổng tiền thuốc trong đơn kê</li>
 *   <li>lab_fee          – tổng tiền xét nghiệm/cận lâm sàng</li>
 *   <li>total_amount     – tổng cộng trước khi áp dụng BHYT</li>
 *   <li>insurance_amount – số tiền BHYT chi trả</li>
 *   <li>paid_amount      – số tiền bệnh nhân thực trả (= total - insurance)</li>
 *   <li>insurance_coverage – mức BHYT áp dụng (FULL/EIGHTY/NONE)</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false, unique = true)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // ── Chi phí thành phần ──────────────────────────────────────────────────────

    @Column(name = "examination_fee", precision = 14, scale = 2)
    private BigDecimal examinationFee = BigDecimal.ZERO;

    @Column(name = "medicine_fee", precision = 14, scale = 2)
    private BigDecimal medicineFee = BigDecimal.ZERO;

    @Column(name = "lab_fee", precision = 14, scale = 2)
    private BigDecimal labFee = BigDecimal.ZERO;

    // ── Tổng hợp & BHYT ─────────────────────────────────────────────────────────

    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_coverage", length = 20)
    private InsuranceCoverage insuranceCoverage = InsuranceCoverage.NONE;

    @Column(name = "insurance_amount", precision = 14, scale = 2)
    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 14, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    // ── Thanh toán ───────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
