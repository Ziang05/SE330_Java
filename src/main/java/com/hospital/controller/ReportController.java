package com.hospital.controller;

import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.RevenueReportResponse;
import com.hospital.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * T42 – REST controller cho module Báo cáo doanh thu.
 *
 * <p>Endpoints:
 * <pre>
 *   GET /api/v1/reports/revenue?from=2026-01-01&to=2026-06-30 – báo cáo theo khoảng ngày
 *   GET /api/v1/reports/revenue/today                          – doanh thu hôm nay
 *   GET /api/v1/reports/revenue/this-month                     – doanh thu tháng này
 * </pre>
 *
 * <p>Chỉ ADMIN mới được truy cập (bảo vệ bằng @PreAuthorize ở từng phương thức).
 * Controller không có try-catch, không trả Entity trực tiếp – tuân thủ Rule.md.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * Báo cáo doanh thu theo khoảng ngày tùy chỉnh.
     *
     * <p>Ví dụ: GET /api/v1/reports/revenue?from=2026-01-01&to=2026-06-30
     *
     * @param from ngày bắt đầu (mặc định: 30 ngày trước)
     * @param to   ngày kết thúc (mặc định: hôm nay)
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // Giá trị mặc định: 30 ngày gần nhất nếu không truyền tham số
        LocalDate endDate   = (to   != null) ? to   : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusDays(29);

        RevenueReportResponse report = reportService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Lấy báo cáo doanh thu thành công", report));
    }

    /**
     * Shortcut: Doanh thu hôm nay.
     * Equivalent to /revenue?from=today&to=today
     */
    @GetMapping("/revenue/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueToday() {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(
                ApiResponse.ok("Báo cáo doanh thu hôm nay",
                        reportService.getRevenueReport(today, today)));
    }

    /**
     * Shortcut: Doanh thu tháng hiện tại (từ ngày 1 đến hôm nay).
     */
    @GetMapping("/revenue/this-month")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueThisMonth() {
        LocalDate today      = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        return ResponseEntity.ok(
                ApiResponse.ok("Bao cao doanh thu thang nay",
                        reportService.getRevenueReport(firstOfMonth, today)));
    }

    /**
     * T43 – Xuất báo cáo doanh thu ra file Excel.
     *
     * <p>Ví dụ: GET /api/v1/reports/revenue/export?from=2026-06-01&to=2026-06-02
     */
    @GetMapping("/revenue/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportRevenueExcel(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate endDate   = (to   != null) ? to   : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusDays(29);

        byte[] excelBytes = reportService.exportRevenueToExcel(startDate, endDate);
        String filename = "revenue-report-" + startDate + "-to-" + endDate + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}
