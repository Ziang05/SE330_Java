package com.hospital.controller;

import com.hospital.dto.request.DoctorRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.DoctorResponse;
import com.hospital.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> create(@Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Doctor created successfully", this.doctorService.create(request)));
    }

    @PutMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> update(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Doctor updated successfully", this.doctorService.update(doctorId, request))
        );
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long doctorId) {
        this.doctorService.delete(doctorId);
        return ResponseEntity.ok(
                ApiResponse.ok("Doctor deleted successfully", null)
        );
    }

    @GetMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> get(@PathVariable Long doctorId) {
        return ResponseEntity.ok(
                ApiResponse.ok(this.doctorService.get(doctorId))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.ok(this.doctorService.getAll())
        );
    }
}
