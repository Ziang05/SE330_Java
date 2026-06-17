package com.hospital.dto.response;

import com.hospital.entity.enums.InvoiceStatus;
import com.hospital.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Một dòng hóa đơn trong lịch sử của bệnh nhân.
 * Hiển thị tất cả trạng thái (PAID, PENDING, CANCELLED).
 * Chỉ các hóa đơn PAID mới được tính vào chi tiêu thực tế.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingInvoiceItem {

    private Long invoiceId;

    /** Ngày tạo hóa đơn (ngay sau khi bác sĩ hoàn thành khám). */
    private LocalDateTime createdAt;

    /** Ngày bệnh nhân thực sự thanh toán (null nếu chưa PAID). */
    private LocalDateTime paidAt;

    // ── Tài chính ────────────────────────────────────────────────────────────────

    private BigDecimal examinationFee;
    private BigDecimal medicineFee;
    private BigDecimal labFee;
    private BigDecimal totalAmount;        // Trước BHYT
    private BigDecimal insuranceAmount;    // BHYT chi trả
    private BigDecimal paidAmount;         // Bệnh nhân thực trả

    // ── Trạng thái ───────────────────────────────────────────────────────────────

    private InvoiceStatus status;
    private PaymentMethod paymentMethod;

    /**
     * Cho biết hóa đơn này có được tính vào thống kê chi tiêu không.
     * true  → đã thanh toán, được tính vào tổng chi tiêu.
     * false → PENDING hoặc CANCELLED, không tính.
     */
    private boolean countedInSpending;
}
