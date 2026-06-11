package com.hospital.util;

import com.hospital.dto.response.PrescriptionResponse;
import com.hospital.dto.response.PrescriptionItemResponse;
import com.hospital.entity.Appointment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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

    public static ByteArrayInputStream generateAppointmentSlip(Appointment appointment) {
        Document document = new Document(PageSize.A5, 30, 30, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            String fontPath = "/fonts/Arial.ttf";
            BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font titleFont = new Font(bf, 16, Font.BOLD);
            Font headerFont = new Font(bf, 11, Font.BOLD);
            Font normalFont = new Font(bf, 10, Font.NORMAL);
            Font italicFont = new Font(bf, 9, Font.ITALIC);

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{70, 30});

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("HỆ THỐNG Y TẾ MEDIPLUS", headerFont));
            leftCell.addElement(new Paragraph("Phân hệ: Quản lý lịch hẹn khám\nHotline: 1900-XXXX", italicFont));
            headerTable.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            String qrData = "APPOINTMENT_ID:" + appointment.getId() + "|PHONE:" + 
                    (appointment.getPatient() != null ? appointment.getPatient().getPhone() : "N/A");
            byte[] qrCodeImageBytes = generateQRCodeImage(qrData, 75, 75);
            
            if (qrCodeImageBytes != null) {
                Image qrImage = Image.getInstance(qrCodeImageBytes);
                qrImage.setAlignment(Element.ALIGN_RIGHT);
                rightCell.addElement(qrImage);
            }
            headerTable.addCell(rightCell);
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            Paragraph title = new Paragraph("PHIẾU HẸN KHÁM BỆNH", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph subTitle = new Paragraph("(Vui lòng xuất trình phiếu này tại quầy tiếp đón)", italicFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);
            document.add(new Paragraph("\n"));

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{40, 60});

            addTableCell(infoTable, "Mã lịch hẹn (ID):", "LH-" + appointment.getId(), headerFont, normalFont);
            addTableCell(infoTable, "Họ và tên bệnh nhân:", appointment.getPatient() != null ? appointment.getPatient().getFullName() : "N/A", headerFont, normalFont);
            addTableCell(infoTable, "Số điện thoại:", appointment.getPatient() != null ? appointment.getPatient().getPhone() : "N/A", headerFont, normalFont);
            addTableCell(infoTable, "Bác sĩ phụ trách:", appointment.getDoctor() != null ? appointment.getDoctor().getFullName() : "N/A", headerFont, normalFont);
            
            String formattedTime = appointment.getApptDatetime() != null ? 
                    appointment.getApptDatetime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
            addTableCell(infoTable, "Thời gian hẹn khám:", formattedTime, headerFont, headerFont);

            document.add(infoTable);
            document.add(new Paragraph("\n\n"));

            Paragraph footerNote = new Paragraph("Lưu ý: Vui lòng có mặt trước giờ hẹn 15 phút tại Quầy tiếp đón để quét mã QR nhận số thứ tự vào phòng khám lâm sàng.", italicFont);
            footerNote.setAlignment(Element.ALIGN_CENTER);
            document.add(footerNote);

            document.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private static byte[] generateQRCodeImage(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addTableCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cellLabel = new PdfPCell(new Paragraph(label, labelFont));
        cellLabel.setPadding(8);
        cellLabel.setBorderWidthBottom(1);
        cellLabel.setBorderWidthTop(0);
        cellLabel.setBorderWidthLeft(0);
        cellLabel.setBorderWidthRight(0);
        
        PdfPCell cellValue = new PdfPCell(new Paragraph(value, valueFont));
        cellValue.setPadding(8);
        cellValue.setBorderWidthBottom(1);
        cellValue.setBorderWidthTop(0);
        cellValue.setBorderWidthLeft(0);
        cellValue.setBorderWidthRight(0);

        table.addCell(cellLabel);
        table.addCell(cellValue);
    }
}
