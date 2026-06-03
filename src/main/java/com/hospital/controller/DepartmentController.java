package com.hospital.controller;

import com.hospital.dto.request.DepartmentRequest;
import com.hospital.dto.request.DoctorRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.DepartmentResponse;
import com.hospital.dto.response.DoctorResponse;
import com.hospital.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Department created successfully", this.departmentService.create(request)));
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable Long departmentId,
            @Valid @RequestBody DepartmentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Department updated successfully", this.departmentService.update(departmentId, request))
        );
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long departmentId) {
        this.departmentService.delete(departmentId);
        return ResponseEntity.ok(
                ApiResponse.ok("Department deleted successfully", null)
        );
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> get(@PathVariable Long departmentId) {
        return ResponseEntity.ok(
                ApiResponse.ok(this.departmentService.get(departmentId))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.ok(this.departmentService.getAll())
        );
    }
}
