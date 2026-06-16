package com.hospital.controller;

import com.hospital.dto.request.PaymentRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.InvoiceResponse;
import com.hospital.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for payment and invoice operations (T39, T40, T44).
 *
 * <p>Endpoints:
 * <pre>
 *   POST /api/v1/invoices/medical-records/{medicalRecordId} – T40: auto-tạo hóa đơn sau khám
 *   POST /api/v1/invoices/pay                               – T39: xác nhận thanh toán
 *   PUT  /api/v1/invoices/medical-records/{id}/recalculate   – tính lại hóa đơn PENDING
 *   GET  /api/v1/invoices/{id}                              – lấy chi tiết hóa đơn
 *   GET  /api/v1/invoices/patient/{patientId}               – lịch sử hóa đơn bệnh nhân
 *   GET  /api/v1/invoices/{id}/export                       – T44: xuất hóa đơn PDF
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoices")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * T40 – Tạo hóa đơn tự động sau khi bác sĩ kết thúc hồ sơ khám.
     */
    @PostMapping("/medical-records/{medicalRecordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR', 'CASHIER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @PathVariable Long medicalRecordId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tao hoa don thanh cong",
                        paymentService.createInvoiceForMedicalRecord(medicalRecordId)));
    }

    /**
     * T39 – Xác nhận thanh toán hóa đơn: PENDING → PAID.
     */
    @PostMapping("/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'CASHIER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Thanh toan thanh cong", paymentService.processPayment(request)));
    }

    /**
     * Tính lại hóa đơn PENDING sau khi bác sĩ đã thêm/xóa prescription hoặc lab test.
     */
    @PutMapping("/medical-records/{medicalRecordId}/recalculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR', 'CASHIER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> recalculateInvoice(
            @PathVariable Long medicalRecordId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Tinh lai hoa don thanh cong",
                        paymentService.recalculateInvoice(medicalRecordId)));
    }

    /**
     * Lấy chi tiết một hóa đơn (để in biên lai, kiểm tra).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR', 'CASHIER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getById(id)));
    }

    /**
     * Lịch sử thanh toán của một bệnh nhân (mới nhất trước).
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getByPatient(
            @PathVariable Long patientId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getByPatientId(patientId)));
    }

    /**
     * T44 – Xuất hóa đơn ra file PDF để in biên lai cho bệnh nhân.
     * Trả về file .pdf, trình duyệt sẽ tự download.
     */
    @GetMapping("/{id}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'CASHIER')")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        byte[] pdfBytes = paymentService.exportInvoiceToPdf(id);
        String filename = "invoice-" + id + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
