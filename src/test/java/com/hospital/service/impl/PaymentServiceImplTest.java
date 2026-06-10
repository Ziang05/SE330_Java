package com.hospital.service.impl;

import com.hospital.dto.request.PaymentRequest;
import com.hospital.dto.response.InvoiceResponse;
import com.hospital.entity.Invoice;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import com.hospital.entity.enums.InsuranceCoverage;
import com.hospital.entity.enums.InvoiceStatus;
import com.hospital.entity.enums.PaymentMethod;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.InvoiceRepository;
import com.hospital.repository.LabTestRepository;
import com.hospital.repository.MedicalRecordRepository;
import com.hospital.repository.PrescriptionItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T45 – Unit Tests cho PaymentServiceImpl.
 *
 * <p>Kiểm thử đầy đủ các nghiệp vụ:
 * <ul>
 *   <li>Tạo hóa đơn tự động (T40): tính phí, xác định mức BHYT, kiểm tra trùng lặp</li>
 *   <li>Xác nhận thanh toán (T39): đổi trạng thái, validate hóa đơn đã trả / đã hủy</li>
 *   <li>Tính toán BHYT: FULL=100%, EIGHTY=80%, NONE=0%</li>
 *   <li>Truy vấn: getById, getByPatientId</li>
 * </ul>
 *
 * <p>Dùng Mockito để giả lập (mock) các Repository –
 * không cần database thật, test chạy hoàn toàn trong RAM.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    // ── Mock các dependency (giả lập Repository) ──────────────────────────────
    @Mock private InvoiceRepository          invoiceRepository;
    @Mock private MedicalRecordRepository    medicalRecordRepository;
    @Mock private PrescriptionItemRepository prescriptionItemRepository;
    @Mock private LabTestRepository          labTestRepository;

    // ── Class cần test – Mockito tự inject các @Mock ở trên vào đây ───────────
    @InjectMocks
    private PaymentServiceImpl paymentService;

    // ── Dữ liệu dùng chung cho nhiều test ─────────────────────────────────────
    private Patient        patientWithInsurance;
    private Patient        patientNoInsurance;
    private MedicalRecord  medicalRecord;
    private Invoice        pendingInvoice;
    private Invoice        paidInvoice;
    private Invoice        cancelledInvoice;

    @BeforeEach
    void setUp() {
        // Bệnh nhân CÓ BHYT
        patientWithInsurance = new Patient();
        patientWithInsurance.setInsuranceNumber("DN123456789");
        patientWithInsurance.setFullName("Test Patient");

        // Bệnh nhân KHÔNG có BHYT
        patientNoInsurance = new Patient();
        patientNoInsurance.setInsuranceNumber(null);
        patientNoInsurance.setFullName("No Insurance Patient");

        // Hồ sơ khám (dùng bệnh nhân có BHYT làm mặc định)
        medicalRecord = new MedicalRecord();
        medicalRecord.setPatient(patientWithInsurance);

        // Hóa đơn ở trạng thái PENDING (chưa thanh toán)
        pendingInvoice = new Invoice();
        pendingInvoice.setId(1L);
        pendingInvoice.setMedicalRecord(medicalRecord);   // Fix: InvoiceMapper cần getMedicalRecord().getId()
        pendingInvoice.setPatient(patientWithInsurance);  // Fix: InvoiceMapper cần getPatient().getId()
        pendingInvoice.setStatus(InvoiceStatus.PENDING);
        pendingInvoice.setTotalAmount(new BigDecimal("442000"));
        pendingInvoice.setInsuranceCoverage(InsuranceCoverage.EIGHTY);
        pendingInvoice.setInsuranceAmount(new BigDecimal("353600"));
        pendingInvoice.setPaidAmount(new BigDecimal("88400"));
        pendingInvoice.setExaminationFee(new BigDecimal("150000"));
        pendingInvoice.setMedicineFee(new BigDecimal("62000"));
        pendingInvoice.setLabFee(new BigDecimal("230000"));

        // Hóa đơn đã thanh toán
        paidInvoice = new Invoice();
        paidInvoice.setId(2L);
        paidInvoice.setMedicalRecord(medicalRecord);
        paidInvoice.setPatient(patientWithInsurance);
        paidInvoice.setStatus(InvoiceStatus.PAID);

        // Hóa đơn đã bị hủy
        cancelledInvoice = new Invoice();
        cancelledInvoice.setId(3L);
        cancelledInvoice.setMedicalRecord(medicalRecord);
        cancelledInvoice.setPatient(patientWithInsurance);
        cancelledInvoice.setStatus(InvoiceStatus.CANCELLED);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // T40: Tạo hóa đơn tự động
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("T40 – Tạo hóa đơn tự động (createInvoiceForMedicalRecord)")
    class CreateInvoiceTests {

        @Test
        @DisplayName("Bệnh nhân có BHYT → BHYT 80%, paidAmount = 20% tổng tiền")
        void createInvoice_withInsurance_applies80PercentCoverage() {
            // GIVEN: phí thuốc 62000, phí xét nghiệm 230000
            // Tổng = 150000 (khám) + 62000 (thuốc) + 230000 (xét nghiệm) = 442000
            // BHYT 80%: insuranceAmount = 442000 * 0.8 = 353600
            // Bệnh nhân trả: 442000 - 353600 = 88400
            when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));
            when(invoiceRepository.findByMedicalRecordId(1L)).thenReturn(Optional.empty());
            when(prescriptionItemRepository.sumMedicineFeByMedicalRecordId(1L))
                    .thenReturn(new BigDecimal("62000"));
            when(labTestRepository.sumLabFeeByMedicalRecordId(1L))
                    .thenReturn(new BigDecimal("230000"));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            InvoiceResponse response = paymentService.createInvoiceForMedicalRecord(1L);

            // THEN
            assertThat(response.getTotalAmount()).isEqualByComparingTo("442000");
            assertThat(response.getInsuranceCoverage()).isEqualTo(InsuranceCoverage.EIGHTY);
            assertThat(response.getInsuranceAmount()).isEqualByComparingTo("353600");
            assertThat(response.getPaidAmount()).isEqualByComparingTo("88400");
            assertThat(response.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        }

        @Test
        @DisplayName("Bệnh nhân KHÔNG có BHYT → NONE, paidAmount = 100% tổng tiền")
        void createInvoice_withoutInsurance_fullPayment() {
            // GIVEN: bệnh nhân không có BHYT
            medicalRecord.setPatient(patientNoInsurance);

            when(medicalRecordRepository.findById(2L)).thenReturn(Optional.of(medicalRecord));
            when(invoiceRepository.findByMedicalRecordId(2L)).thenReturn(Optional.empty());
            when(prescriptionItemRepository.sumMedicineFeByMedicalRecordId(2L))
                    .thenReturn(BigDecimal.ZERO);
            when(labTestRepository.sumLabFeeByMedicalRecordId(2L))
                    .thenReturn(BigDecimal.ZERO);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            InvoiceResponse response = paymentService.createInvoiceForMedicalRecord(2L);

            // THEN: phí khám 150000, không có thuốc, không có xét nghiệm → trả đúng 150000
            assertThat(response.getTotalAmount()).isEqualByComparingTo("150000");
            assertThat(response.getInsuranceCoverage()).isEqualTo(InsuranceCoverage.NONE);
            assertThat(response.getInsuranceAmount()).isEqualByComparingTo("0");
            assertThat(response.getPaidAmount()).isEqualByComparingTo("150000");
        }

        @Test
        @DisplayName("Hóa đơn đã tồn tại → ném BusinessException")
        void createInvoice_alreadyExists_throwsBusinessException() {
            // GIVEN: hóa đơn đã được tạo trước đó
            when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));
            when(invoiceRepository.findByMedicalRecordId(1L))
                    .thenReturn(Optional.of(pendingInvoice));

            // WHEN + THEN
            assertThatThrownBy(() -> paymentService.createInvoiceForMedicalRecord(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Hóa đơn đã tồn tại");

            // Đảm bảo không gọi save khi đã tồn tại hóa đơn
            verify(invoiceRepository, never()).save(any());
        }

        @Test
        @DisplayName("MedicalRecord không tồn tại → ném ResourceNotFoundException")
        void createInvoice_medicalRecordNotFound_throwsResourceNotFoundException() {
            // GIVEN: không tìm thấy hồ sơ khám
            when(medicalRecordRepository.findById(99L)).thenReturn(Optional.empty());

            // WHEN + THEN
            assertThatThrownBy(() -> paymentService.createInvoiceForMedicalRecord(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Phí khám luôn là 150,000đ cố định bất kể loại bệnh nhân")
        void createInvoice_examinationFeeIsAlways150000() {
            when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));
            when(invoiceRepository.findByMedicalRecordId(1L)).thenReturn(Optional.empty());
            when(prescriptionItemRepository.sumMedicineFeByMedicalRecordId(1L))
                    .thenReturn(BigDecimal.ZERO);
            when(labTestRepository.sumLabFeeByMedicalRecordId(1L))
                    .thenReturn(BigDecimal.ZERO);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

            InvoiceResponse response = paymentService.createInvoiceForMedicalRecord(1L);

            assertThat(response.getExaminationFee()).isEqualByComparingTo("150000");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // T39: Xác nhận thanh toán
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("T39 – Xác nhận thanh toán (processPayment)")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Thanh toán hóa đơn PENDING → chuyển sang PAID thành công")
        void processPayment_pendingInvoice_statusBecomePaid() {
            // GIVEN
            PaymentRequest request = new PaymentRequest();
            request.setInvoiceId(1L);
            request.setPaymentMethod(PaymentMethod.CASH);

            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(pendingInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            InvoiceResponse response = paymentService.processPayment(request);

            // THEN
            assertThat(response.getStatus()).isEqualTo(InvoiceStatus.PAID);
            assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
            assertThat(response.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("Thanh toán hóa đơn đã PAID → ném BusinessException")
        void processPayment_alreadyPaid_throwsBusinessException() {
            // GIVEN
            PaymentRequest request = new PaymentRequest();
            request.setInvoiceId(2L);
            request.setPaymentMethod(PaymentMethod.CASH);

            when(invoiceRepository.findById(2L)).thenReturn(Optional.of(paidInvoice));

            // WHEN + THEN
            assertThatThrownBy(() -> paymentService.processPayment(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("đã được thanh toán rồi");

            verify(invoiceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Thanh toán hóa đơn đã CANCELLED → ném BusinessException")
        void processPayment_cancelledInvoice_throwsBusinessException() {
            // GIVEN
            PaymentRequest request = new PaymentRequest();
            request.setInvoiceId(3L);
            request.setPaymentMethod(PaymentMethod.TRANSFER);

            when(invoiceRepository.findById(3L)).thenReturn(Optional.of(cancelledInvoice));

            // WHEN + THEN
            assertThatThrownBy(() -> paymentService.processPayment(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("đã bị hủy");

            verify(invoiceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Invoice không tồn tại → ném ResourceNotFoundException")
        void processPayment_invoiceNotFound_throwsResourceNotFoundException() {
            // GIVEN
            PaymentRequest request = new PaymentRequest();
            request.setInvoiceId(999L);
            request.setPaymentMethod(PaymentMethod.CASH);

            when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

            // WHEN + THEN
            assertThatThrownBy(() -> paymentService.processPayment(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Phương thức thanh toán TRANSFER được ghi nhận đúng")
        void processPayment_transferMethod_recordedCorrectly() {
            PaymentRequest request = new PaymentRequest();
            request.setInvoiceId(1L);
            request.setPaymentMethod(PaymentMethod.TRANSFER);

            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(pendingInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

            InvoiceResponse response = paymentService.processPayment(request);

            assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.TRANSFER);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tính toán BHYT (Insurance Calculation Logic)
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Logic tính BHYT – Kiểm thử biên (boundary) công thức")
    class InsuranceCalculationTests {

        /**
         * Helper: tạo hóa đơn với tổng tiền cho trước, không có BHYT cũ.
         * Sau đó kiểm tra paidAmount sau khi service tính toán.
         */
        private InvoiceResponse invokeCreateWith(Patient patient, BigDecimal medicineFee, BigDecimal labFee) {
            medicalRecord.setPatient(patient);
            when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.of(medicalRecord));
            when(invoiceRepository.findByMedicalRecordId(anyLong())).thenReturn(Optional.empty());
            when(prescriptionItemRepository.sumMedicineFeByMedicalRecordId(anyLong())).thenReturn(medicineFee);
            when(labTestRepository.sumLabFeeByMedicalRecordId(anyLong())).thenReturn(labFee);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
            return paymentService.createInvoiceForMedicalRecord(1L);
        }

        @Test
        @DisplayName("BHYT 80% – Số lẻ được làm tròn XUỐNG (RoundingMode.DOWN)")
        void insurance80Percent_roundsDown() {
            // Tổng = 150000 + 1 (thuốc) + 0 = 150001
            // BHYT 80% = 150001 * 0.8 = 120000.8 → làm tròn xuống = 120000
            // Bệnh nhân trả: 150001 - 120000 = 30001
            InvoiceResponse response = invokeCreateWith(
                    patientWithInsurance,
                    BigDecimal.ONE,      // medicineFee = 1đ để tạo số lẻ
                    BigDecimal.ZERO
            );

            assertThat(response.getInsuranceAmount()).isEqualByComparingTo("120000");
            assertThat(response.getPaidAmount()).isEqualByComparingTo("30001");
        }

        @Test
        @DisplayName("BHYT NONE – Bệnh nhân trả 100% tổng tiền")
        void insuranceNone_patientPaysFullAmount() {
            // Tổng = 150000 + 50000 + 0 = 200000, không BHYT
            InvoiceResponse response = invokeCreateWith(
                    patientNoInsurance,
                    new BigDecimal("50000"),
                    BigDecimal.ZERO
            );

            assertThat(response.getInsuranceAmount()).isEqualByComparingTo("0");
            assertThat(response.getPaidAmount()).isEqualByComparingTo("200000");
        }

        @Test
        @DisplayName("Tổng tiền = examinationFee + medicineFee + labFee (cộng đúng)")
        void totalAmount_isSumOfAllFees() {
            InvoiceResponse response = invokeCreateWith(
                    patientWithInsurance,
                    new BigDecimal("100000"),  // thuốc
                    new BigDecimal("200000")   // xét nghiệm
            );
            // Tổng = 150000 + 100000 + 200000 = 450000
            assertThat(response.getTotalAmount()).isEqualByComparingTo("450000");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Truy vấn: getById, getByPatientId
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Truy vấn hóa đơn (getById, getByPatientId)")
    class QueryTests {

        @Test
        @DisplayName("getById tìm thấy → trả về InvoiceResponse đúng ID")
        void getById_found_returnsResponse() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(pendingInvoice));

            InvoiceResponse response = paymentService.getById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        }

        @Test
        @DisplayName("getById không tìm thấy → ném ResourceNotFoundException")
        void getById_notFound_throwsResourceNotFoundException() {
            when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("getByPatientId không có hóa đơn → trả về danh sách rỗng")
        void getByPatientId_noInvoices_returnsEmptyList() {
            when(invoiceRepository.findByPatientIdOrderByCreatedAtDesc(1L))
                    .thenReturn(java.util.List.of());

            var result = paymentService.getByPatientId(1L);

            assertThat(result).isEmpty();
        }
    }
}
