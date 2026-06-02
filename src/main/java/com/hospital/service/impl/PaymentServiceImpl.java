package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.PaymentRequest;
import com.hospital.dto.response.InvoiceResponse;
import com.hospital.entity.Invoice;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.enums.InsuranceCoverage;
import com.hospital.entity.enums.InvoiceStatus;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.InvoiceRepository;
import com.hospital.repository.LabTestRepository;
import com.hospital.repository.MedicalRecordRepository;
import com.hospital.repository.PrescriptionItemRepository;
import com.hospital.service.PaymentService;
import com.hospital.util.InvoiceMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implements payment and invoice creation logic (T39, T40).
 *
 * <p>Công thức BHYT áp dụng:
 * <ul>
 *   <li>Bệnh nhân có insuranceNumber  → InsuranceCoverage.EIGHTY  (80% tổng tiền)</li>
 *   <li>Bệnh nhân không có BHYT       → InsuranceCoverage.NONE    (0%, tự trả 100%)</li>
 * </ul>
 *
 * <p>Phí khám cố định được cấu hình qua hằng số EXAMINATION_FEE.
 * Có thể chuyển sang @ConfigurationProperties sau nếu cần linh hoạt hơn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    /** Phí khám bác sĩ cố định (VNĐ). */
    private static final BigDecimal EXAMINATION_FEE = new BigDecimal("150000");

    /** Tỷ lệ BHYT chi trả (80%). */
    private static final BigDecimal INSURANCE_RATE = new BigDecimal("0.80");

    private final InvoiceRepository          invoiceRepository;
    private final MedicalRecordRepository    medicalRecordRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final LabTestRepository          labTestRepository;

    // ── T40: AUTO-TẠO HÓA ĐƠN SAU KHI KHÁM XONG ─────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = "CREATE", entityType = "Invoice")
    public InvoiceResponse createInvoiceForMedicalRecord(Long medicalRecordId) {
        MedicalRecord record = findMedicalRecord(medicalRecordId);

        // Kiểm tra hóa đơn đã tồn tại chưa (mỗi medical record chỉ có 1 hóa đơn)
        invoiceRepository.findByMedicalRecordId(medicalRecordId).ifPresent(existing -> {
            throw new BusinessException(
                    "Hóa đơn đã tồn tại cho hồ sơ khám này (Invoice ID: " + existing.getId() + ")");
        });

        // 1. Tính phí từng phần
        BigDecimal medicineFee = prescriptionItemRepository
                .sumMedicineFeByMedicalRecordId(medicalRecordId);
        BigDecimal labFee = labTestRepository
                .sumLabFeeByMedicalRecordId(medicalRecordId);
        BigDecimal totalAmount = EXAMINATION_FEE.add(medicineFee).add(labFee);

        // 2. Xác định mức BHYT dựa trên thông tin bệnh nhân
        String insuranceNumber = record.getPatient().getInsuranceNumber();
        InsuranceCoverage coverage = (insuranceNumber != null && !insuranceNumber.isBlank())
                ? InsuranceCoverage.EIGHTY
                : InsuranceCoverage.NONE;

        // 3. Tính tiền BHYT chi trả và tiền bệnh nhân thực trả
        BigDecimal insuranceAmount = calculateInsuranceAmount(totalAmount, coverage);
        BigDecimal paidAmount = totalAmount.subtract(insuranceAmount);

        // 4. Tạo và lưu hóa đơn
        Invoice invoice = new Invoice();
        invoice.setMedicalRecord(record);
        invoice.setPatient(record.getPatient());
        invoice.setExaminationFee(EXAMINATION_FEE);
        invoice.setMedicineFee(medicineFee);
        invoice.setLabFee(labFee);
        invoice.setTotalAmount(totalAmount);
        invoice.setInsuranceCoverage(coverage);
        invoice.setInsuranceAmount(insuranceAmount);
        invoice.setPaidAmount(paidAmount);
        invoice.setStatus(InvoiceStatus.PENDING);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice auto-created: id={}, medicalRecordId={}, total={}, paidAmount={}",
                saved.getId(), medicalRecordId, totalAmount, paidAmount);
        return InvoiceMapper.toResponse(saved);
    }

    // ── T39: XÁC NHẬN THANH TOÁN ─────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = "UPDATE", entityType = "Invoice")
    public InvoiceResponse processPayment(PaymentRequest request) {
        Invoice invoice = findInvoice(request.getInvoiceId());

        // Validate trạng thái: chỉ PENDING mới được thanh toán
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Hóa đơn #" + invoice.getId() + " đã được thanh toán rồi.");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Hóa đơn #" + invoice.getId() + " đã bị hủy, không thể thanh toán.");
        }

        // Cập nhật thông tin thanh toán
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice paid: id={}, method={}, paidAt={}", saved.getId(), saved.getPaymentMethod(), saved.getPaidAt());
        return InvoiceMapper.toResponse(saved);
    }

    // ── READ ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long invoiceId) {
        return InvoiceMapper.toResponse(findInvoice(invoiceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByPatientId(Long patientId) {
        return invoiceRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────────

    /**
     * Tính tiền BHYT chi trả dựa trên mức coverage.
     * FULL=100%, EIGHTY=80%, NONE=0%.
     * Làm tròn xuống đơn vị đồng (scale=0, RoundingMode.DOWN).
     */
    private BigDecimal calculateInsuranceAmount(BigDecimal totalAmount, InsuranceCoverage coverage) {
        return switch (coverage) {
            case FULL    -> totalAmount;
            case EIGHTY  -> totalAmount.multiply(INSURANCE_RATE).setScale(0, RoundingMode.DOWN);
            case NONE    -> BigDecimal.ZERO;
        };
    }

    private Invoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    private MedicalRecord findMedicalRecord(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", id));
    }

    // ── T44: XUẤT HOÁ ĐƠN PDF ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] exportInvoiceToPdf(Long invoiceId) {
        Invoice invoice = findInvoice(invoiceId);
        InvoiceResponse data = InvoiceMapper.toResponse(invoice);
        log.info("Exporting invoice PDF: id={}", invoiceId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 60, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            buildPdfHeader(doc);
            buildPdfPatientInfo(doc, data);
            buildPdfFeeTable(doc, data);
            buildPdfFooter(doc, data);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new BusinessException("Khong the tao file PDF: " + e.getMessage());
        }
    }

    /** In tiêu đề bệnh viện ở đầu trang. */
    private void buildPdfHeader(Document doc) throws Exception {
        Font hospitalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Font subFont      = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);

        Paragraph hospital = new Paragraph("HOSPITAL MANAGEMENT SYSTEM", hospitalFont);
        hospital.setAlignment(Element.ALIGN_CENTER);
        doc.add(hospital);

        Paragraph sub = new Paragraph("Payment Receipt", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);
        doc.add(new Paragraph(" "));
    }

    /** In thông tin bệnh nhân và hóa đơn. */
    private void buildPdfPatientInfo(Document doc, InvoiceResponse data) throws Exception {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        doc.add(createInfoLine("Invoice ID    : ", String.valueOf(data.getId()), labelFont, valueFont));
        doc.add(createInfoLine("Patient       : ", data.getPatientName(), labelFont, valueFont));
        doc.add(createInfoLine("Medical Record: ", String.valueOf(data.getMedicalRecordId()), labelFont, valueFont));
        doc.add(createInfoLine("Status        : ", data.getStatus().name(), labelFont, valueFont));
        if (data.getPaidAt() != null) {
            doc.add(createInfoLine("Paid At       : ", data.getPaidAt().format(fmt), labelFont, valueFont));
        }
        if (data.getPaymentMethod() != null) {
            doc.add(createInfoLine("Payment Method: ", data.getPaymentMethod().name(), labelFont, valueFont));
        }
        doc.add(new Paragraph(" "));
    }

    /** Bảng chi tiết phí. */
    private void buildPdfFeeTable(Document doc, InvoiceResponse data) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font cellFont   = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 2f});

        // Header row
        addTableCell(table, "Fee Type",         headerFont, new Color(30, 60, 114),  true);
        addTableCell(table, "Amount (VND)",      headerFont, new Color(30, 60, 114),  true);

        // Fee rows
        addTableCell(table, "Examination Fee",   cellFont,   Color.WHITE,             false);
        addTableCell(table, data.getExaminationFee().toPlainString(), cellFont, Color.WHITE, false);

        addTableCell(table, "Medicine Fee",      cellFont,   new Color(240,240,240),  false);
        addTableCell(table, data.getMedicineFee().toPlainString(), cellFont, new Color(240,240,240), false);

        addTableCell(table, "Lab Test Fee",      cellFont,   Color.WHITE,             false);
        addTableCell(table, data.getLabFee().toPlainString(),      cellFont, Color.WHITE,            false);

        // Subtotal
        addTableCell(table, "Total Amount",      headerFont, new Color(220,220,220),  false);
        addTableCell(table, data.getTotalAmount().toPlainString(),  headerFont, new Color(220,220,220), false);

        // Insurance
        String coverageStr = data.getInsuranceCoverage() != null ? data.getInsuranceCoverage().name() : "NONE";
        addTableCell(table, "Insurance (" + coverageStr + ")", cellFont, Color.WHITE, false);
        addTableCell(table, "- " + data.getInsuranceAmount().toPlainString(), cellFont, Color.WHITE, false);

        // Paid amount – highlighted
        addTableCell(table, "PATIENT PAYS",      headerFont, new Color(30, 60, 114),  true);
        addTableCell(table, data.getPaidAmount().toPlainString(),   headerFont, new Color(30, 60, 114), true);

        doc.add(table);
    }

    /** In footer "Thank you" ở cuối. */
    private void buildPdfFooter(Document doc, InvoiceResponse data) throws Exception {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
        doc.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Thank you for using our service. - Generated automatically.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    private Paragraph createInfoLine(String label, String value, Font labelFont, Font valueFont) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(label, labelFont));
        p.add(new Phrase(value, valueFont));
        return p;
    }

    private void addTableCell(PdfPTable table, String text, Font font, Color bgColor, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(isHeader ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
        table.addCell(cell);
    }
}
