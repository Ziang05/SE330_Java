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
import java.util.List;

/**
 * Response DTO for an invoice, returned to client after create or payment.
 */
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

    // ── Chi tiết các mục đã tính phí ──────────────────────────────────────────────
    private List<LabTestLineItem> labTests;
    private List<PrescriptionLineItem> prescriptions;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LabTestLineItem {
        private Long id;
        private String testName;
        private String description;
        private BigDecimal fee;
        private String status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PrescriptionLineItem {
        private Long prescriptionId;
        private String doctorName;
        private String doctorNotes;
        private List<MedicineLineItem> medicines;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class MedicineLineItem {
        private Long id;
        private String medicationName;
        private Integer quantity;
        private String unit;
        private String dosage;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
