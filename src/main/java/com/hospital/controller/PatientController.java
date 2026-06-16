package com.hospital.controller;

import com.hospital.dto.request.PatientRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.PatientResponse;
import com.hospital.service.PatientService;
import com.hospital.util.PatientExcelUtil;
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
import java.util.List;

/**
 * Sample REST controller for patient CRUD.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

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
