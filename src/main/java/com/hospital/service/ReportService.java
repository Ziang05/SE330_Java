package com.hospital.service;

import com.hospital.dto.response.RevenueReportResponse;

import java.time.LocalDate;

/**
 * T42 – Business contract cho module Báo cáo doanh thu.
 * T43 – Xuất báo cáo Excel.
 *
 * <p>Chỉ ADMIN mới có quyền gọi các API này (bảo vệ tại Controller bằng @PreAuthorize).
 */
public interface ReportService {

    /**
     * T42 – Lấy báo cáo doanh thu theo khoảng thời gian.
     *
     * @param from ngày bắt đầu (inclusive)
     * @param to   ngày kết thúc (inclusive)
     * @return {@link RevenueReportResponse} tổng hợp + chi tiết ngày
     */
    RevenueReportResponse getRevenueReport(LocalDate from, LocalDate to);

    /**
     * T43 – Xuất báo cáo doanh thu ra file Excel (.xlsx).
     *
     * <p>File gồm 2 sheet:
     * <ul>
     *   <li>Sheet "Summary" – tổng doanh thu, tổng lượt khám, tổng BHYT trong kỳ</li>
     *   <li>Sheet "Daily Breakdown" – chi tiết từng ngày (Date, Revenue, Insurance, Visits)</li>
     * </ul>
     *
     * @param from ngày bắt đầu
     * @param to   ngày kết thúc
     * @return mảng byte của file .xlsx (sẵn sàng ghi vào HTTP response)
     */
    byte[] exportRevenueToExcel(LocalDate from, LocalDate to);
}
