package com.hospital.controller;

import com.hospital.dto.request.PrescriptionRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.PrescriptionResponse;
import com.hospital.service.PrescriptionService;
import com.hospital.util.PdfGeneratorUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

/**
 * REST Controller quản lý các nghiệp vụ liên quan đến Đơn thuốc (Prescription).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * API Kê đơn thuốc mới cho bệnh nhân.
     * Quyền truy cập: Chỉ ADMIN hoặc DOCTOR (Bác sĩ điều trị) mới được phép kê đơn.
     * URL: POST /api/v1/prescriptions
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ApiResponse<PrescriptionResponse> createPrescription(@Valid @RequestBody PrescriptionRequest request) {
        log.info("REST request - Khởi tạo đơn thuốc cho MedicalRecord ID: {}", request.getMedicalRecordId());
        PrescriptionResponse response = prescriptionService.createPrescription(request);
        return ApiResponse.ok("Kê đơn thuốc thành công!", response);
    }

    /**
     * API Lấy thông tin chi tiết của một đơn thuốc.
     * Quyền truy cập: Bác sĩ, Y tá và Admin đều có thể xem để phối hợp phát thuốc/hướng dẫn.
     * URL: GET /api/v1/prescriptions/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ApiResponse<PrescriptionResponse> getPrescriptionById(@PathVariable Long id) {
        log.info("REST request - Lấy chi tiết đơn thuốc ID: {}", id);
        PrescriptionResponse response = prescriptionService.getPrescriptionById(id);
        return ApiResponse.ok("Tải thông tin đơn thuốc thành công!", response);
    }

    /**
     * API Đặc biệt: Xuất đơn thuốc ra file PDF để in ấn trực tiếp tại phòng khám.
     * Quyền truy cập: Bác sĩ, Y tá hoặc Admin.
     * URL: GET /api/v1/prescriptions/{id}/export-pdf
     */
    @GetMapping(value = "/{id}/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<InputStreamResource> exportPrescriptionPdf(@PathVariable Long id) {
        log.info("REST request - Xuất file PDF cho đơn thuốc ID: {}", id);
        
        PrescriptionResponse prescription = prescriptionService.getPrescriptionById(id);
        
        ByteArrayInputStream pdfStream = PdfGeneratorUtil.generatePrescriptionPdf(prescription);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=Prescription_" + id + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}
