package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.PaymentRequest;
import com.hospital.dto.response.InvoiceResponse;
import com.hospital.entity.Invoice;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.enums.InsuranceCoverage;
import com.hospital.entity.enums.InvoiceStatus;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.InvoiceRepository;
import com.hospital.repository.LabTestRepository;
import com.hospital.repository.MedicalRecordRepository;
import com.hospital.repository.PrescriptionItemRepository;
import com.hospital.service.PaymentService;
import com.hospital.util.InvoiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implements payment and invoice creation logic (T39, T40).
 *
 * <p>Công thức BHYT áp dụng:
 * <ul>
 *   <li>Bệnh nhân có insuranceNumber  → InsuranceCoverage.EIGHTY  (80% tổng tiền)</li>
 *   <li>Bệnh nhân không có BHYT       → InsuranceCoverage.NONE    (0%, tự trả 100%)</li>
 * </ul>
 *
 * <p>Phí khám cố định được cấu hình qua hằng số EXAMINATION_FEE.
 * Có thể chuyển sang @ConfigurationProperties sau nếu cần linh hoạt hơn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    /** Phí khám bác sĩ cố định (VNĐ). */
    private static final BigDecimal EXAMINATION_FEE = new BigDecimal("150000");

    /** Tỷ lệ BHYT chi trả (80%). */
    private static final BigDecimal INSURANCE_RATE = new BigDecimal("0.80");

    private final InvoiceRepository          invoiceRepository;
    private final MedicalRecordRepository    medicalRecordRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final LabTestRepository          labTestRepository;

    // ── T40: AUTO-TẠO HÓA ĐƠN SAU KHI KHÁM XONG ─────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = "CREATE", entityType = "Invoice")
    public InvoiceResponse createInvoiceForMedicalRecord(Long medicalRecordId) {
        MedicalRecord record = findMedicalRecord(medicalRecordId);

        // Kiểm tra hóa đơn đã tồn tại chưa (mỗi medical record chỉ có 1 hóa đơn)
        invoiceRepository.findByMedicalRecordId(medicalRecordId).ifPresent(existing -> {
            throw new BusinessException(
                    "Hóa đơn đã tồn tại cho hồ sơ khám này (Invoice ID: " + existing.getId() + ")");
        });

        // 1. Tính phí từng phần
        BigDecimal medicineFee = prescriptionItemRepository
                .sumMedicineFeByMedicalRecordId(medicalRecordId);
        BigDecimal labFee = labTestRepository
                .sumLabFeeByMedicalRecordId(medicalRecordId);
        BigDecimal totalAmount = EXAMINATION_FEE.add(medicineFee).add(labFee);

        // 2. Xác định mức BHYT dựa trên thông tin bệnh nhân
        String insuranceNumber = record.getPatient().getInsuranceNumber();
        InsuranceCoverage coverage = (insuranceNumber != null && !insuranceNumber.isBlank())
                ? InsuranceCoverage.EIGHTY
                : InsuranceCoverage.NONE;

        // 3. Tính tiền BHYT chi trả và tiền bệnh nhân thực trả
        BigDecimal insuranceAmount = calculateInsuranceAmount(totalAmount, coverage);
        BigDecimal paidAmount = totalAmount.subtract(insuranceAmount);

        // 4. Tạo và lưu hóa đơn
        Invoice invoice = new Invoice();
        invoice.setMedicalRecord(record);
        invoice.setPatient(record.getPatient());
        invoice.setExaminationFee(EXAMINATION_FEE);
        invoice.setMedicineFee(medicineFee);
        invoice.setLabFee(labFee);
        invoice.setTotalAmount(totalAmount);
        invoice.setInsuranceCoverage(coverage);
        invoice.setInsuranceAmount(insuranceAmount);
        invoice.setPaidAmount(paidAmount);
        invoice.setStatus(InvoiceStatus.PENDING);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice auto-created: id={}, medicalRecordId={}, total={}, paidAmount={}",
                saved.getId(), medicalRecordId, totalAmount, paidAmount);
        return InvoiceMapper.toResponse(saved);
    }

    // ── T39: XÁC NHẬN THANH TOÁN ─────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = "UPDATE", entityType = "Invoice")
    public InvoiceResponse processPayment(PaymentRequest request) {
        Invoice invoice = findInvoice(request.getInvoiceId());

        // Validate trạng thái: chỉ PENDING mới được thanh toán
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Hóa đơn #" + invoice.getId() + " đã được thanh toán rồi.");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Hóa đơn #" + invoice.getId() + " đã bị hủy, không thể thanh toán.");
        }

        // Cập nhật thông tin thanh toán
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice paid: id={}, method={}, paidAt={}", saved.getId(), saved.getPaymentMethod(), saved.getPaidAt());
        return InvoiceMapper.toResponse(saved);
    }

    // ── READ ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long invoiceId) {
        return InvoiceMapper.toResponse(findInvoice(invoiceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByPatientId(Long patientId) {
        return invoiceRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────────

    /**
     * Tính tiền BHYT chi trả dựa trên mức coverage.
     * FULL=100%, EIGHTY=80%, NONE=0%.
     * Làm tròn xuống đơn vị đồng (scale=0, RoundingMode.DOWN).
     */
    private BigDecimal calculateInsuranceAmount(BigDecimal totalAmount, InsuranceCoverage coverage) {
        return switch (coverage) {
            case FULL    -> totalAmount;
            case EIGHTY  -> totalAmount.multiply(INSURANCE_RATE).setScale(0, RoundingMode.DOWN);
            case NONE    -> BigDecimal.ZERO;
        };
    }

    private Invoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    private MedicalRecord findMedicalRecord(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", id));
    }
}
