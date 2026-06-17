package com.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tổng hợp chi tiêu của bệnh nhân trong một khoảng thời gian.
 * Chỉ tính các hóa đơn có trạng thái PAID.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingSummaryResponse {

    // ── Thông tin bệnh nhân ──────────────────────────────────────────────────────

    private Long   patientId;
    private String patientName;

    // ── Tổng hợp tài chính (chỉ tính hóa đơn PAID) ──────────────────────────────

    /** Tổng số tiền bệnh nhân thực trả (sau khi trừ BHYT). */
    private BigDecimal totalSpent;

    /** Tổng số tiền BHYT chi trả thay bệnh nhân. */
    private BigDecimal totalInsuranceCovered;

    /** Tổng tiền trước khi áp dụng BHYT (= totalSpent + totalInsuranceCovered). */
    private BigDecimal totalBilled;

    // ── Phân loại phí (chỉ tính hóa đơn PAID) ────────────────────────────────────

    private BigDecimal examinationFeeTotal;
    private BigDecimal medicineFeeTotal;
    private BigDecimal labFeeTotal;

    // ── Thống kê số lượng hóa đơn ────────────────────────────────────────────────

    /** Tổng số hóa đơn trong khoảng thời gian (tất cả trạng thái). */
    private int totalInvoices;

    /** Số hóa đơn đã thanh toán → được tính vào chi tiêu. */
    private int paidCount;

    /** Số hóa đơn đang chờ thanh toán. */
    private int pendingCount;

    /** Số hóa đơn đã hủy. */
    private int cancelledCount;

    // ── Khoảng thời gian query ────────────────────────────────────────────────────

    private LocalDate from;
    private LocalDate to;
}
