package com.hospital.dto.response;

import com.hospital.entity.enums.InsuranceCoverage;
import com.hospital.entity.enums.InvoiceStatus;
import com.hospital.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Response DTO for an invoice, returned to client after create or payment. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Long id;

    private Long medicalRecordId;
    private Long patientId;
    private String patientName;

    // ── Chi phí thành phần ──────────────────────────────────────────────────────
    private BigDecimal examinationFee;
    private BigDecimal medicineFee;
    private BigDecimal labFee;

    // ── BHYT ────────────────────────────────────────────────────────────────────
    private BigDecimal totalAmount;
    private InsuranceCoverage insuranceCoverage;
    private BigDecimal insuranceAmount;
    private BigDecimal paidAmount;

    // ── Thanh toán ───────────────────────────────────────────────────────────────
    private PaymentMethod paymentMethod;
    private InvoiceStatus status;
    private LocalDateTime paidAt;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
