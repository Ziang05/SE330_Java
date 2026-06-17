package com.hospital.service.impl;

import com.hospital.dto.response.SpendingInvoiceItem;
import com.hospital.dto.response.SpendingSummaryResponse;
import com.hospital.entity.Invoice;
import com.hospital.entity.Patient;
import com.hospital.entity.enums.InvoiceStatus;
import com.hospital.entity.enums.PaymentMethod;
import com.hospital.exception.BusinessException;
import com.hospital.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit Tests cho PatientSpendingServiceImpl.
 *
 * <p>Kiểm thử các nghiệp vụ:
 * <ul>
 *   <li>Chỉ hóa đơn PAID được tính vào chi tiêu</li>
 *   <li>Hóa đơn PENDING và CANCELLED không tính vào tổng</li>
 *   <li>Giá trị mặc định khi không truyền ngày (1 năm gần nhất)</li>
 *   <li>Validate khoảng ngày: from sau to</li>
 *   <li>Validate khoảng ngày: quá 365 ngày</li>
 *   <li>patientId = null → BusinessException (400)</li>
 *   <li>Không có hóa đơn → trả về zero thay vì lỗi</li>
 *   <li>Flag countedInSpending đúng với từng trạng thái</li>
 *   <li>Hỗn hợp nhiều trạng thái — tổng hợp đúng</li>
 * </ul>
 *
 * <p>Dùng Mockito mock InvoiceRepository — không cần database thật.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientSpendingServiceImpl Tests")
class PatientSpendingServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PatientSpendingServiceImpl spendingService;

    // ── Dữ liệu mẫu dùng chung ───────────────────────────────────────────────────

    private static final Long PATIENT_ID = 1L;
    private static final LocalDate FROM   = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO     = LocalDate.of(2026, 6, 17);

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setFullName("Nguyen Van A");
    }

    // ── Helper: tạo Invoice nhanh ─────────────────────────────────────────────────

    private Invoice buildInvoice(Long id,
                                  InvoiceStatus status,
                                  BigDecimal examFee,
                                  BigDecimal medFee,
                                  BigDecimal labFee,
                                  BigDecimal insuranceAmount) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setPatient(patient);
        invoice.setStatus(status);
        invoice.setExaminationFee(examFee);
        invoice.setMedicineFee(medFee);
        invoice.setLabFee(labFee);

        BigDecimal total = examFee.add(medFee).add(labFee);
        invoice.setTotalAmount(total);
        invoice.setInsuranceAmount(insuranceAmount);
        invoice.setPaidAmount(total.subtract(insuranceAmount));

        if (status == InvoiceStatus.PAID) {
            invoice.setPaymentMethod(PaymentMethod.CASH);
            invoice.setPaidAt(LocalDateTime.of(2026, 3, 15, 10, 0));
        }

        // Giả lập createdAt bằng reflection (BaseEntity dùng @PrePersist)
        try {
            var field = invoice.getClass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(invoice, LocalDateTime.of(2026, 3, 1, 9, 0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return invoice;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // TEST GROUP 1: getSummary — Logic chi tiêu
    // ═══════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSummary — Logic tổng hợp chi tiêu")
    class GetSummaryTests {

        @Test
        @DisplayName("Chỉ tính hóa đơn PAID vào tổng chi tiêu")
        void getSummary_onlyCountsPaidInvoices() {
            // Arrange
            Invoice paid = buildInvoice(1L, InvoiceStatus.PAID,
                    new BigDecimal("150000"), new BigDecimal("80000"), new BigDecimal("50000"),
                    new BigDecimal("224000"));  // BHYT 80% của 280000

            Invoice pending = buildInvoice(2L, InvoiceStatus.PENDING,
                    new BigDecimal("150000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO);

            Invoice cancelled = buildInvoice(3L, InvoiceStatus.CANCELLED,
                    new BigDecimal("200000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO);

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(List.of(paid, pending, cancelled));

            // Act
            SpendingSummaryResponse summary = spendingService.getSummary(PATIENT_ID, FROM, TO);

            // Assert: tổng chi tiêu chỉ tính từ hóa đơn PAID
            assertThat(summary.getTotalSpent())
                    .isEqualByComparingTo(paid.getPaidAmount());
            assertThat(summary.getTotalInsuranceCovered())
                    .isEqualByComparingTo(paid.getInsuranceAmount());
            assertThat(summary.getTotalBilled())
                    .isEqualByComparingTo(paid.getTotalAmount());

            // Assert: đếm đúng số lượng từng trạng thái
            assertThat(summary.getTotalInvoices()).isEqualTo(3);
            assertThat(summary.getPaidCount()).isEqualTo(1);
            assertThat(summary.getPendingCount()).isEqualTo(1);
            assertThat(summary.getCancelledCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Phân loại phí đúng: examFee + medicineFee + labFee")
        void getSummary_feeBreakdownCorrect() {
            // Arrange
            Invoice paid = buildInvoice(1L, InvoiceStatus.PAID,
                    new BigDecimal("150000"),  // examFee
                    new BigDecimal("62000"),   // medicineFee
                    new BigDecimal("230000"),  // labFee
                    new BigDecimal("353600")); // insuranceAmount (80%)

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(List.of(paid));

            // Act
            SpendingSummaryResponse summary = spendingService.getSummary(PATIENT_ID, FROM, TO);

            // Assert
            assertThat(summary.getExaminationFeeTotal()).isEqualByComparingTo("150000");
            assertThat(summary.getMedicineFeeTotal()).isEqualByComparingTo("62000");
            assertThat(summary.getLabFeeTotal()).isEqualByComparingTo("230000");
            assertThat(summary.getTotalBilled()).isEqualByComparingTo("442000");
        }

        @Test
        @DisplayName("Không có hóa đơn → trả về tất cả zero, không ném lỗi")
        void getSummary_noInvoices_returnsZeros() {
            // Arrange
            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            SpendingSummaryResponse summary = spendingService.getSummary(PATIENT_ID, FROM, TO);

            // Assert
            assertThat(summary.getTotalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getTotalInsuranceCovered()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getTotalBilled()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getTotalInvoices()).isZero();
            assertThat(summary.getPaidCount()).isZero();
        }

        @Test
        @DisplayName("Nhiều hóa đơn PAID → tổng hợp đúng")
        void getSummary_multiplePaidInvoices_aggregatesCorrectly() {
            // Arrange: 2 hóa đơn PAID
            Invoice paid1 = buildInvoice(1L, InvoiceStatus.PAID,
                    new BigDecimal("150000"), new BigDecimal("50000"), BigDecimal.ZERO,
                    new BigDecimal("160000"));  // 80% của 200000

            Invoice paid2 = buildInvoice(2L, InvoiceStatus.PAID,
                    new BigDecimal("150000"), BigDecimal.ZERO, new BigDecimal("100000"),
                    new BigDecimal("200000"));  // 80% của 250000

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(List.of(paid1, paid2));

            // Act
            SpendingSummaryResponse summary = spendingService.getSummary(PATIENT_ID, FROM, TO);

            // Assert: tổng exam fee = 150000 + 150000 = 300000
            assertThat(summary.getExaminationFeeTotal()).isEqualByComparingTo("300000");
            assertThat(summary.getPaidCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Tên bệnh nhân được điền đúng từ hóa đơn đầu tiên")
        void getSummary_patientNameFilledCorrectly() {
            Invoice paid = buildInvoice(1L, InvoiceStatus.PAID,
                    new BigDecimal("150000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO);

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(List.of(paid));

            SpendingSummaryResponse summary = spendingService.getSummary(PATIENT_ID, FROM, TO);

            assertThat(summary.getPatientName()).isEqualTo("Nguyen Van A");
            assertThat(summary.getPatientId()).isEqualTo(PATIENT_ID);
        }

        @Test
        @DisplayName("Khoảng thời gian được ghi đúng vào response")
        void getSummary_dateRangeStoredInResponse() {
            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            SpendingSummaryResponse summary = spendingService.getSummary(PATIENT_ID, FROM, TO);

            assertThat(summary.getFrom()).isEqualTo(FROM);
            assertThat(summary.getTo()).isEqualTo(TO);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // TEST GROUP 2: getInvoiceHistory — Flag countedInSpending
    // ═══════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getInvoiceHistory — Flag countedInSpending")
    class GetInvoiceHistoryTests {

        @Test
        @DisplayName("Hóa đơn PAID → countedInSpending = true")
        void getInvoiceHistory_paidInvoice_countedInSpendingTrue() {
            Invoice paid = buildInvoice(1L, InvoiceStatus.PAID,
                    new BigDecimal("150000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO);

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(List.of(paid));

            List<SpendingInvoiceItem> items = spendingService.getInvoiceHistory(PATIENT_ID, FROM, TO);

            assertThat(items).hasSize(1);
            assertThat(items.get(0).isCountedInSpending()).isTrue();
            assertThat(items.get(0).getStatus()).isEqualTo(InvoiceStatus.PAID);
        }

        @Test
        @DisplayName("Hóa đơn PENDING → countedInSpending = false")
        void getInvoiceHistory_pendingInvoice_countedInSpendingFalse() {
            Invoice pending = buildInvoice(2L, InvoiceStatus.PENDING,
                    new BigDecimal("150000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO);

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(List.of(pending));

            List<SpendingInvoiceItem> items = spendingService.getInvoiceHistory(PATIENT_ID, FROM, TO);

            assertThat(items.get(0).isCountedInSpending()).isFalse();
            assertThat(items.get(0).getStatus()).isEqualTo(InvoiceStatus.PENDING);
        }

        @Test
        @DisplayName("Hóa đơn CANCELLED → countedInSpending = false")
        void getInvoiceHistory_cancelledInvoice_countedInSpendingFalse() {
            Invoice cancelled = buildInvoice(3L, InvoiceStatus.CANCELLED,
                    new BigDecimal("200000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO);

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(List.of(cancelled));

            List<SpendingInvoiceItem> items = spendingService.getInvoiceHistory(PATIENT_ID, FROM, TO);

            assertThat(items.get(0).isCountedInSpending()).isFalse();
            assertThat(items.get(0).getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        }

        @Test
        @DisplayName("Hỗn hợp 3 trạng thái → trả về tất cả, flag đúng")
        void getInvoiceHistory_mixedStatuses_allReturnedWithCorrectFlags() {
            Invoice paid      = buildInvoice(1L, InvoiceStatus.PAID,
                    new BigDecimal("150000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            Invoice pending   = buildInvoice(2L, InvoiceStatus.PENDING,
                    new BigDecimal("150000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            Invoice cancelled = buildInvoice(3L, InvoiceStatus.CANCELLED,
                    new BigDecimal("150000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(List.of(paid, pending, cancelled));

            List<SpendingInvoiceItem> items = spendingService.getInvoiceHistory(PATIENT_ID, FROM, TO);

            assertThat(items).hasSize(3);
            assertThat(items.get(0).isCountedInSpending()).isTrue();
            assertThat(items.get(1).isCountedInSpending()).isFalse();
            assertThat(items.get(2).isCountedInSpending()).isFalse();
        }

        @Test
        @DisplayName("Map đúng tất cả field sang SpendingInvoiceItem")
        void getInvoiceHistory_fieldsMappedCorrectly() {
            Invoice paid = buildInvoice(1L, InvoiceStatus.PAID,
                    new BigDecimal("150000"),
                    new BigDecimal("62000"),
                    new BigDecimal("230000"),
                    new BigDecimal("353600"));

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(List.of(paid));

            List<SpendingInvoiceItem> items = spendingService.getInvoiceHistory(PATIENT_ID, FROM, TO);
            SpendingInvoiceItem item = items.get(0);

            assertThat(item.getInvoiceId()).isEqualTo(1L);
            assertThat(item.getExaminationFee()).isEqualByComparingTo("150000");
            assertThat(item.getMedicineFee()).isEqualByComparingTo("62000");
            assertThat(item.getLabFee()).isEqualByComparingTo("230000");
            assertThat(item.getTotalAmount()).isEqualByComparingTo("442000");
            assertThat(item.getInsuranceAmount()).isEqualByComparingTo("353600");
            assertThat(item.getPaidAmount()).isEqualByComparingTo("88400");
            assertThat(item.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // TEST GROUP 3: Validation — patientId và khoảng ngày
    // ═══════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Validation — patientId và khoảng ngày")
    class ValidationTests {

        @Test
        @DisplayName("patientId = null → ném BusinessException (400)")
        void getSummary_nullPatientId_throwsBusinessException() {
            assertThatThrownBy(() -> spendingService.getSummary(null, FROM, TO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("chưa liên kết hồ sơ bệnh nhân");
        }

        @Test
        @DisplayName("patientId = null cho getInvoiceHistory → ném BusinessException (400)")
        void getInvoiceHistory_nullPatientId_throwsBusinessException() {
            assertThatThrownBy(() -> spendingService.getInvoiceHistory(null, FROM, TO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("chưa liên kết hồ sơ bệnh nhân");
        }

        @Test
        @DisplayName("from sau to → ném BusinessException")
        void getSummary_fromAfterTo_throwsBusinessException() {
            LocalDate invalidFrom = LocalDate.of(2026, 6, 17);
            LocalDate invalidTo   = LocalDate.of(2026, 1, 1);

            assertThatThrownBy(() -> spendingService.getSummary(PATIENT_ID, invalidFrom, invalidTo))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        @Test
        @DisplayName("Khoảng thời gian quá 365 ngày → ném BusinessException")
        void getSummary_rangeOver365Days_throwsBusinessException() {
            LocalDate bigFrom = LocalDate.of(2024, 1, 1);
            LocalDate bigTo   = LocalDate.of(2026, 6, 17); // > 365 ngày

            assertThatThrownBy(() -> spendingService.getSummary(PATIENT_ID, bigFrom, bigTo))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("365 ngày");
        }

        @Test
        @DisplayName("from = null → dùng 1 năm trước hôm nay, không ném lỗi")
        void getSummary_nullFrom_usesDefaultOneYearAgo() {
            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Không ném exception khi from = null
            SpendingSummaryResponse summary = spendingService.getSummary(PATIENT_ID, null, null);

            assertThat(summary).isNotNull();
            assertThat(summary.getFrom()).isEqualTo(LocalDate.now().minusYears(1));
            assertThat(summary.getTo()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("from = to (cùng ngày) → hợp lệ, không ném lỗi")
        void getSummary_fromEqualsTo_valid() {
            LocalDate sameDay = LocalDate.of(2026, 6, 17);

            when(invoiceRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(PATIENT_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            SpendingSummaryResponse summary = spendingService.getSummary(PATIENT_ID, sameDay, sameDay);

            assertThat(summary).isNotNull();
        }
    }
}
