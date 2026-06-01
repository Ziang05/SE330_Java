package com.hospital.service;

import com.hospital.dto.request.AuditLogFilterRequest;
import com.hospital.dto.response.AuditLogResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;

public interface AuditLogService {

    Page<AuditLogResponse> getLogs(AuditLogFilterRequest filter);

    AuditLogResponse getById(Long id);

    void exportCsv(AuditLogFilterRequest filter, HttpServletResponse response);
}
