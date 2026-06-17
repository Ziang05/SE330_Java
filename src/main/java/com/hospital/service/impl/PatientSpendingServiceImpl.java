package com.hospital.service.impl;

import com.hospital.dto.response.SpendingInvoiceItem;
import com.hospital.dto.response.SpendingSummaryResponse;
import com.hospital.entity.Invoice;
import com.hospital.entity.enums.InvoiceStatus;
import com.hospital.exception.BusinessException;
import com.hospital.repository.InvoiceRepository;
import com.hospital.service.PatientSpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Thực thi logic thống kê chi tiêu cho bệnh nhân.
 *
 * <p>Quy tắc nghiệp vụ chính:
 * <ul>
 *   <li>Tổng hợp chi tiêu (getSummary) chỉ tính hóa đơn PAID.</li>
 *   <li>Lịch sử (getInvoiceHistory) trả về tất cả trạng thái,
 *       nhưng đánh dấu flag countedInSpending = true chỉ cho hóa đơn PAID.</li>
 *   <li>Ngày lọc dùng createdAt (ngày hóa đơn được tạo).</li>
 *   <li>Khoảng thời gian tối đa 365 ngày.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientSpendingServiceImpl implements PatientSpendingService {

    private static final int MAX_DAYS = 365;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final InvoiceRepository invoiceRepository;

    // ── getSummary ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public SpendingSummaryResponse getSummary(Long patientId, LocalDate from, LocalDate to) {
        validatePatientId(patientId);

        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusYears(1);
        LocalDate effectiveTo   = to   != null ? to   : LocalDate.now();
        validateDateRange(effectiveFrom, effectiveTo);

        LocalDateTime start = effectiveFrom.atStartOfDay();
        LocalDateTime end   = effectiveTo.atTime(23, 59, 59);

        List<Invoice> invoices = invoiceRepository
                .findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(patientId, start, end);

        log.info("Patient spending summary: patientId={}, from={}, to={}, invoices={}",
                patientId, effectiveFrom, effectiveTo, invoices.size());

        return buildSummary(patientId, invoices, effectiveFrom, effectiveTo);
    }

    // ── getInvoiceHistory ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SpendingInvoiceItem> getInvoiceHistory(Long patientId, LocalDate from, LocalDate to) {
        validatePatientId(patientId);

        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusYears(1);
        LocalDate effectiveTo   = to   != null ? to   : LocalDate.now();
        validateDateRange(effectiveFrom, effectiveTo);

        LocalDateTime start = effectiveFrom.atStartOfDay();
        LocalDateTime end   = effectiveTo.atTime(23, 59, 59);

        List<Invoice> invoices = invoiceRepository
                .findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(patientId, start, end);

        log.info("Patient invoice history: patientId={}, from={}, to={}, invoices={}",
                patientId, effectiveFrom, effectiveTo, invoices.size());

        return invoices.stream()
                .map(this::toSpendingItem)
                .toList();
    }

    // ── Private Helpers ───────────────────────────────────────────────────────────

    /**
     * Xây dựng SpendingSummaryResponse từ danh sách hóa đơn.
     * Chỉ các hóa đơn PAID mới được tính vào tổng chi tiêu.
     */
    private SpendingSummaryResponse buildSummary(Long patientId,
                                                  List<Invoice> invoices,
                                                  LocalDate from, LocalDate to) {
        // Phân loại theo trạng thái
        List<Invoice> paid      = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.PAID).toList();
        long pendingCount    = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.PENDING).count();
        long cancelledCount  = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.CANCELLED).count();

        // Chỉ tính tổng từ hóa đơn PAID
        BigDecimal totalSpent     = sumField(paid, Invoice::getPaidAmount);
        BigDecimal totalInsurance = sumField(paid, Invoice::getInsuranceAmount);
        BigDecimal totalBilled    = sumField(paid, Invoice::getTotalAmount);
        BigDecimal examFee        = sumField(paid, Invoice::getExaminationFee);
        BigDecimal medicineFee    = sumField(paid, Invoice::getMedicineFee);
        BigDecimal labFee         = sumField(paid, Invoice::getLabFee);

        // Lấy tên bệnh nhân từ hóa đơn đầu tiên (nếu có)
        String patientName = invoices.isEmpty() ? null
                : invoices.get(0).getPatient().getFullName();

        return SpendingSummaryResponse.builder()
                .patientId(patientId)
                .patientName(patientName)
                .totalSpent(totalSpent)
                .totalInsuranceCovered(totalInsurance)
                .totalBilled(totalBilled)
                .examinationFeeTotal(examFee)
                .medicineFeeTotal(medicineFee)
                .labFeeTotal(labFee)
                .totalInvoices(invoices.size())
                .paidCount(paid.size())
                .pendingCount((int) pendingCount)
                .cancelledCount((int) cancelledCount)
                .from(from)
                .to(to)
                .build();
    }

    /**
     * Map Invoice → SpendingInvoiceItem.
     * Đánh dấu countedInSpending = true nếu trạng thái là PAID.
     */
    private SpendingInvoiceItem toSpendingItem(Invoice invoice) {
        return SpendingInvoiceItem.builder()
                .invoiceId(invoice.getId())
                .createdAt(invoice.getCreatedAt())
                .paidAt(invoice.getPaidAt())
                .examinationFee(invoice.getExaminationFee())
                .medicineFee(invoice.getMedicineFee())
                .labFee(invoice.getLabFee())
                .totalAmount(invoice.getTotalAmount())
                .insuranceAmount(invoice.getInsuranceAmount())
                .paidAmount(invoice.getPaidAmount())
                .status(invoice.getStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .countedInSpending(invoice.getStatus() == InvoiceStatus.PAID)
                .build();
    }

    /**
     * Tiện ích tính tổng một field BigDecimal từ danh sách hóa đơn.
     */
    private BigDecimal sumField(List<Invoice> invoices,
                                java.util.function.Function<Invoice, BigDecimal> fieldExtractor) {
        return invoices.stream()
                .map(fieldExtractor)
                .filter(v -> v != null)
                .reduce(ZERO, BigDecimal::add);
    }

    /**
     * Kiểm tra patientId hợp lệ.
     * Trả 400 nếu tài khoản chưa liên kết hồ sơ bệnh nhân.
     */
    private void validatePatientId(Long patientId) {
        if (patientId == null) {
            throw new BusinessException(
                    "Tài khoản chưa liên kết hồ sơ bệnh nhân. Vui lòng liên hệ nhân viên bệnh viện.");
        }
    }

    /**
     * Validate khoảng thời gian query.
     */
    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("Ngày bắt đầu không thể sau ngày kết thúc.");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_DAYS) {
            throw new BusinessException("Khoảng thời gian tối đa là " + MAX_DAYS + " ngày.");
        }
    }
}
