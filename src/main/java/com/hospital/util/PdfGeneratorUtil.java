package com.hospital.util;

import com.hospital.dto.response.PrescriptionResponse;
import com.hospital.dto.response.PrescriptionItemResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

public class PdfGeneratorUtil {

    public static ByteArrayInputStream generatePrescriptionPdf(PrescriptionResponse prescription) {
        Document document = new Document(PageSize.A5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            String fontPath = "/fonts/Arial.ttf";
            
            BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            
            Font titleFont = new Font(bf, 16, Font.BOLD);
            Font headerFont = new Font(bf, 10, Font.BOLD);
            Font normalFont = new Font(bf, 10, Font.NORMAL);
            Font italicFont = new Font(bf, 9, Font.ITALIC);
            
            Paragraph title = new Paragraph("ĐƠN THUỐC\n(PRESCRIPTION)", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            document.add(new Paragraph("Mã đơn thuốc: " + prescription.getId(), normalFont));
            document.add(new Paragraph("Bệnh nhân: " + prescription.getPatientName(), normalFont));
            document.add(new Paragraph("Bác sĩ kê đơn: " + prescription.getDoctorName(), normalFont));
            
            String dateStr = prescription.getCreatedAt() != null ? 
                    prescription.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
            document.add(new Paragraph("Ngày kê đơn: " + dateStr, normalFont));
            
            document.add(new Paragraph("----------------------------------------------------------------------------------", normalFont));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new float[]{1, 4, 1.5f, 4.5f});

            String[] headers = {"STT", "Tên thuốc", "Số lượng", "Liều lượng & Cách dùng"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            int stt = 1;
            for (PrescriptionItemResponse item : prescription.getItems()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(stt++), normalFont)));
                table.addCell(new PdfPCell(new Phrase(item.getMedicationName(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(item.getQuantity() + " " + (item.getUnit() != null ? item.getUnit() : "viên"), normalFont)));
                table.addCell(new PdfPCell(new Phrase(item.getDosage(), normalFont)));
            }
            document.add(table);

            if (prescription.getDoctorNotes() != null && !prescription.getDoctorNotes().isEmpty()) {
                Paragraph notes = new Paragraph("\nLời dặn của bác sĩ: " + prescription.getDoctorNotes(), italicFont);
                document.add(notes);
            }

            Paragraph signature = new Paragraph("\n\n\nBác sĩ kê đơn\n(Ký và ghi rõ họ tên)", normalFont);
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

            document.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
