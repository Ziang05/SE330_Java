package com.hospital.service.impl;

import com.hospital.dto.response.RevenueReportResponse;
import com.hospital.dto.response.RevenueReportResponse.DailySummary;
import com.hospital.exception.BusinessException;
import com.hospital.repository.InvoiceRepository;
import com.hospital.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * T42 – Triển khai logic báo cáo doanh thu.
 *
 * <p>Luồng xử lý:
 * <ol>
 *   <li>Validate khoảng ngày đầu vào.</li>
 *   <li>Query DB: tổng doanh thu, tổng lượt khám, chi tiết từng ngày.</li>
 *   <li>Map raw Object[] từ JPQL sang {@link DailySummary}.</li>
 *   <li>Tổng hợp thành {@link RevenueReportResponse}.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    /** Giới hạn tối đa kỳ báo cáo – tránh query lấy hàng triệu dòng. */
    private static final long MAX_DAYS = 365;

    private final InvoiceRepository invoiceRepository;

    @Override
    public RevenueReportResponse getRevenueReport(LocalDate from, LocalDate to) {

        // ── 1. Validate đầu vào ─────────────────────────────────────────────────
        if (from.isAfter(to)) {
            throw new BusinessException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        if (from.plusDays(MAX_DAYS).isBefore(to)) {
            throw new BusinessException("Khoảng báo cáo không được vượt quá " + MAX_DAYS + " ngày");
        }

        log.info("Generating revenue report from {} to {}", from, to);

        // ── 2. Chuyển LocalDate → LocalDateTime để query ─────────────────────────
        // from: đầu ngày (00:00:00)  |  to: cuối ngày (23:59:59)
        LocalDateTime startDt = from.atStartOfDay();
        LocalDateTime endDt   = to.atTime(LocalTime.MAX);

        // ── 3. Lấy dữ liệu từ Repository ────────────────────────────────────────
        BigDecimal totalRevenue  = invoiceRepository.sumRevenueBetween(startDt, endDt);
        long       totalVisits   = invoiceRepository.countVisitsBetween(startDt, endDt);

        // Tổng BHYT chi trả: dùng lại sumInsuranceBetween (sẽ thêm query vào Repository)
        BigDecimal totalInsurance = invoiceRepository.sumInsuranceBetween(startDt, endDt);

        // ── 4. Map chi tiết từng ngày ────────────────────────────────────────────
        List<Object[]> rawDaily = invoiceRepository.revenueGroupedByDay(startDt, endDt);
        List<DailySummary> dailyBreakdown = rawDaily.stream()
                .map(row -> new DailySummary(
                        // row[0]: java.sql.Date hoặc LocalDate (tùy driver MySQL)
                        toLocalDate(row[0]),
                        toBigDecimal(row[1]),
                        toLong(row[2])
                ))
                .toList();

        log.info("Revenue report generated: totalRevenue={}, totalVisits={}", totalRevenue, totalVisits);

        // ── 5. Đóng gói kết quả ─────────────────────────────────────────────────
        return new RevenueReportResponse(
                from,
                to,
                totalRevenue,
                totalInsurance,
                totalVisits,
                dailyBreakdown
        );
    }

    // ── Private helpers (chuyển đổi kiểu từ raw JPQL Object[]) ─────────────────

    private LocalDate toLocalDate(Object obj) {
        if (obj instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (obj instanceof LocalDate ld) {
            return ld;
        }
        // MySQL FUNCTION('DATE') đôi khi trả về String dạng "yyyy-MM-dd"
        return LocalDate.parse(obj.toString());
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal bd) return bd;
        return new BigDecimal(obj.toString());
    }

    private long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number n) return n.longValue();
        return Long.parseLong(obj.toString());
    }

    // ── T43: Xuất Excel ────────────────────────────────────────────────────────

    @Override
    public byte[] exportRevenueToExcel(LocalDate from, LocalDate to) {
        // Lấy dữ liệu (validate + query cùng logic T42)
        RevenueReportResponse report = getRevenueReport(from, to);
        log.info("Exporting revenue Excel from {} to {}", from, to);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            buildSummarySheet(workbook, report);
            buildDailySheet(workbook, report);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new BusinessException("Khong the tao file Excel: " + e.getMessage());
        }
    }

    /** Sheet 1: "Summary" – tóm tắt tổng kỳ báo cáo. */
    private void buildSummarySheet(Workbook wb, RevenueReportResponse report) {
        Sheet sheet = wb.createSheet("Summary");
        sheet.setColumnWidth(0, 9000);
        sheet.setColumnWidth(1, 6000);

        CellStyle titleStyle  = makeTitleStyle(wb);
        CellStyle headerStyle = makeHeaderStyle(wb);
        CellStyle dataStyle   = makeDataStyle(wb);

        // Tiêu đề
        Row title = sheet.createRow(0);
        createCell(title, 0, "Revenue Report: " + report.getFromDate() + " to " + report.getToDate(), titleStyle);

        // Header
        Row header = sheet.createRow(2);
        createCell(header, 0, "Metric",             headerStyle);
        createCell(header, 1, "Value",               headerStyle);

        // Data rows
        String[][] rows = {
                {"Total Revenue (VND)",    report.getTotalRevenue().toPlainString()},
                {"Total Insurance (VND)",  report.getTotalInsuranceAmount().toPlainString()},
                {"Total Visits",           String.valueOf(report.getTotalVisits())},
                {"From Date",              report.getFromDate().toString()},
                {"To Date",                report.getToDate().toString()},
        };

        for (int i = 0; i < rows.length; i++) {
            Row row = sheet.createRow(i + 3);
            createCell(row, 0, rows[i][0], dataStyle);
            createCell(row, 1, rows[i][1], dataStyle);
        }
    }

    /** Sheet 2: "Daily Breakdown" – chi tiết từng ngày. */
    private void buildDailySheet(Workbook wb, RevenueReportResponse report) {
        Sheet sheet = wb.createSheet("Daily Breakdown");
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 4000);

        CellStyle headerStyle = makeHeaderStyle(wb);
        CellStyle dataStyle   = makeDataStyle(wb);

        // Header row
        Row header = sheet.createRow(0);
        createCell(header, 0, "Date",          headerStyle);
        createCell(header, 1, "Revenue (VND)", headerStyle);
        createCell(header, 2, "Visits",        headerStyle);

        // Data rows
        int rowIdx = 1;
        for (RevenueReportResponse.DailySummary day : report.getDailyBreakdown()) {
            Row row = sheet.createRow(rowIdx++);
            createCell(row, 0, day.getDate().toString(),             dataStyle);
            createCell(row, 1, day.getRevenue().toPlainString(),     dataStyle);
            createCell(row, 2, String.valueOf(day.getVisits()),      dataStyle);
        }
    }

    // ── Excel style helpers ───────────────────────────────────────────────────

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle makeTitleStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle makeHeaderStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle makeDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
