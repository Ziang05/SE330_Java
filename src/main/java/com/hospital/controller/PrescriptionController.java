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
 * REST Controller qu岷� l媒 c谩c nghi峄噋 v峄� li锚n quan 膽岷縩 膼啤n thu峄慶 (Prescription).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * API K锚 膽啤n thu峄慶 m峄沬 cho b峄噉h nh芒n.
     * Quy峄乶 truy c岷璸: Ch峄� ADMIN ho岷穋 DOCTOR (B谩c s末 膽i峄乽 tr峄�) m峄沬 膽瓢峄� ph茅p k锚 膽啤n.
     * URL: POST /api/v1/prescriptions
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ApiResponse<PrescriptionResponse> createPrescription(@Valid @RequestBody PrescriptionRequest request) {
        log.info("REST request - Kh峄焛 t岷� 膽啤n thu峄慶 cho MedicalRecord ID: {}", request.getMedicalRecordId());
        PrescriptionResponse response = prescriptionService.createPrescription(request);
        return ApiResponse.ok("K锚 膽啤n thu峄慶 th脿nh c么ng!", response);
    }

    /**
     * API L岷� th么ng tin chi ti岷縯 c峄� m峄檛 膽啤n thu峄慶.
     * Quy峄乶 truy c岷璸: B谩c s末, Y t谩 v脿 Admin 膽峄乽 c贸 th峄� xem 膽峄� ph峄慽 h峄� ph谩t thu峄慶/h瓢峄沶g d岷玭.
     * URL: GET /api/v1/prescriptions/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ApiResponse<PrescriptionResponse> getPrescriptionById(@PathVariable Long id) {
        log.info("REST request - L岷� chi ti岷縯 膽啤n thu峄慶 ID: {}", id);
        PrescriptionResponse response = prescriptionService.getPrescriptionById(id);
        return ApiResponse.ok("T岷� th么ng tin 膽啤n thu峄慶 th脿nh c么ng!", response);
    }

    /**
     * API 膼岷穋 bi峄噒: Xu岷� 膽啤n thu峄慶 ra file PDF 膽峄� in 岷� tr峄眂 ti岷縫 t岷� ph貌ng kh谩m.
     * Quy峄乶 truy c岷璸: B谩c s末, Y t谩 ho岷穋 Admin.
     * URL: GET /api/v1/prescriptions/{id}/export-pdf
     */
    @GetMapping(value = "/{id}/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<InputStreamResource> exportPrescriptionPdf(@PathVariable Long id) {
        log.info("REST request - Xu岷� file PDF cho 膽啤n thu峄慶 ID: {}", id);
        
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

    /**
     * API lay danh sach don thuoc cua mot benh nhan theo patient ID.
     * Dung de ho tro tim kiem Prescription ID tren frontend.
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ApiResponse<java.util.List<PrescriptionResponse>> getPrescriptionsByPatientId(@PathVariable Long patientId) {
        log.info("REST request - Lay danh sach don thuoc cua Patient ID: {}", patientId);
        java.util.List<PrescriptionResponse> response = prescriptionService.getPrescriptionsByPatientId(patientId);
        return ApiResponse.ok("Tai danh sach don thuoc thanh cong.", response);
    }
}