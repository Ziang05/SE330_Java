package com.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * T42 – Báo cáo doanh thu trả về cho client.
 *
 * <p>Gồm 2 phần:
 * <ul>
 *   <li>{@link #summary} – Tổng hợp toàn kỳ (tổng tiền, tổng lượt khám...)</li>
 *   <li>{@link #dailyBreakdown} – Chi tiết từng ngày (dùng để vẽ biểu đồ)</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {

    /** Ngày bắt đầu kỳ báo cáo (inclusive). */
    private LocalDate fromDate;

    /** Ngày kết thúc kỳ báo cáo (inclusive). */
    private LocalDate toDate;

    /** Tổng doanh thu thu được trong kỳ (tổng paid_amount). */
    private BigDecimal totalRevenue;

    /** Tổng số tiền BHYT chi trả trong kỳ. */
    private BigDecimal totalInsuranceAmount;

    /** Tổng lượt khám (số hóa đơn PAID) trong kỳ. */
    private long totalVisits;

    /** Chi tiết doanh thu từng ngày trong kỳ. */
    private List<DailySummary> dailyBreakdown;

    // ── Inner class ────────────────────────────────────────────────────────────

    /**
     * Doanh thu của một ngày cụ thể – dùng để render biểu đồ đường / cột.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySummary {

        /** Ngày thống kê. */
        private LocalDate date;

        /** Tổng tiền thu về trong ngày (paid_amount). */
        private BigDecimal revenue;

        /** Số lượt khám (hóa đơn PAID) trong ngày. */
        private long visits;
    }
}
