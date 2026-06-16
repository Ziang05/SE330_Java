package com.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO đóng gói dữ liệu Hồ sơ khám bệnh được sinh ra sau khi tiếp nhận thành công.
 */
@Getter
@Setter
@NoArgsConstructor
public class MedicalRecordResponse {
    
    private Long id;
    private Long appointmentId;
    private LocalDate visitDate;
    
    private String status;        
    
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String patientInsuranceNumber;

    private Long doctorId;
    private String doctorName;
    private String departmentName;
    
    private LocalDateTime createdAt;

    // ── Related data ─────────────────────────────────────────────────────────────
    private List<LabTestSummary> labTests;
    private List<PrescriptionSummary> prescriptions;
    private InvoiceSummary invoice;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LabTestSummary {
        private Long id;
        private String testName;
        private String description;
        private BigDecimal fee;
        private String status;
        private String result;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PrescriptionSummary {
        private Long id;
        private String doctorName;
        private LocalDate issuedDate;
        private String status;
        private List<MedicineItem> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class MedicineItem {
        private Long id;
        private String medicineName;
        private Integer quantity;
        private String unit;
        private String dosage;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class InvoiceSummary {
        private Long id;
        private BigDecimal examinationFee;
        private BigDecimal medicineFee;
        private BigDecimal labFee;
        private BigDecimal totalAmount;
        private BigDecimal insuranceAmount;
        private BigDecimal paidAmount;
        private String status;
    }
}
