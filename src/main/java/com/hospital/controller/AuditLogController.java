package com.hospital.controller;

import com.hospital.dto.request.AuditLogFilterRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.AuditLogResponse;
import com.hospital.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Log")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get audit logs with filters")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogs(@ModelAttribute AuditLogFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.ok(auditLogService.getLogs(filter)));
    }

    @GetMapping("/export")
    @Operation(summary = "Export audit logs as CSV")
    public void exportCsv(@ModelAttribute AuditLogFilterRequest filter, HttpServletResponse response) {
        auditLogService.exportCsv(filter, response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by id")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(auditLogService.getById(id)));
    }
}
