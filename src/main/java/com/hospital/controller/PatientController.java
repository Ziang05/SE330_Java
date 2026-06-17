package com.hospital.controller;

import com.hospital.dto.request.PatientRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.PatientResponse;
import com.hospital.dto.response.SpendingInvoiceItem;
import com.hospital.dto.response.SpendingSummaryResponse;
import com.hospital.security.UserPrincipal;
import com.hospital.service.PatientService;
import com.hospital.service.PatientSpendingService;
import com.hospital.util.PatientExcelUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Sample REST controller for patient CRUD.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;
    private final PatientSpendingService patientSpendingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<ApiResponse<PatientResponse>> create(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Patient created successfully", patientService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'CASHIER')")
    public ResponseEntity<ApiResponse<PatientResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getAll()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.searchByName(keyword)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<ApiResponse<PatientResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Patient updated successfully", patientService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Patient deleted successfully", null));
    }

    // ── Patient self-service: Thống kê chi tiêu ──────────────────────────────────

    /**
     * Bệnh nhân xem tổng hợp chi tiêu của chính mình.
     * patientId được lấy từ JWT token — bệnh nhân không thể xem của người khác.
     */
    @GetMapping("/me/spending")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<SpendingSummaryResponse>> getMySpending(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        SpendingSummaryResponse summary =
                patientSpendingService.getSummary(principal.getPatientId(), from, to);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    /**
     * Bệnh nhân xem lịch sử toàn bộ hóa đơn của chính mình (mọi trạng thái).
     * Mỗi item có flag countedInSpending = true nếu hóa đơn đã PAID.
     */
    @GetMapping("/me/invoices")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<SpendingInvoiceItem>>> getMyInvoices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        List<SpendingInvoiceItem> items =
                patientSpendingService.getInvoiceHistory(principal.getPatientId(), from, to);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<InputStreamResource> exportPatients(@RequestParam(required = false) String keyword) {
        List<PatientResponse> patients;
        if (keyword != null && !keyword.trim().isEmpty()) {
            patients = patientService.searchByName(keyword);
        } else {
            patients = patientService.getAll();
        }

        ByteArrayInputStream excelFile = PatientExcelUtil.exportToExcel(patients);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=patients_export.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excelFile));
    }
}
