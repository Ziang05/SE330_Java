package com.hospital.util;

import com.hospital.dto.response.PatientResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for exporting patient data to Excel format.
 */
public class PatientExcelUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] HEADERS = {
            "ID", "Full Name", "Date of Birth", "Gender", "CCCD", "Phone", "Address", "Blood Type", "Insurance Number"
    };

    public static ByteArrayInputStream exportToExcel(List<PatientResponse> patients) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Patients");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowIndex = 1;
            for (PatientResponse patient : patients) {
                Row row = sheet.createRow(rowIndex++);

                createCell(row, 0, patient.getId() != null ? patient.getId().toString() : "", dataStyle);
                createCell(row, 1, patient.getFullName() != null ? patient.getFullName() : "", dataStyle);
                createCell(row, 2, patient.getDob() != null ? patient.getDob().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 3, patient.getGender() != null ? patient.getGender().toString() : "", dataStyle);
                createCell(row, 4, patient.getCccd() != null ? patient.getCccd() : "", dataStyle);
                createCell(row, 5, patient.getPhone() != null ? patient.getPhone() : "", dataStyle);
                createCell(row, 6, patient.getAddress() != null ? patient.getAddress() : "", dataStyle);
                createCell(row, 7, patient.getBloodType() != null ? patient.getBloodType() : "", dataStyle);
                createCell(row, 8, patient.getInsuranceNumber() != null ? patient.getInsuranceNumber() : "", dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export patient list to Excel", e);
        }
    }

    private static void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
