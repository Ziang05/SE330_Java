package com.hospital.service.impl;

import com.hospital.dto.request.AuditLogFilterRequest;
import com.hospital.dto.response.AuditLogResponse;
import com.hospital.entity.AuditLog;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.AuditLogRepository;
import com.hospital.service.AuditLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final DateTimeFormatter CSV_FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogs(AuditLogFilterRequest filter) {
        validateDateRange(filter);
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        return auditLogRepository.findByFilters(
                        filter.getUserId(),
                        normalize(filter.getAction()),
                        normalize(filter.getEntityType()),
                        filter.getFromDate(),
                        filter.getToDate(),
                        pageable
                )
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getById(Long id) {
        return auditLogRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public void exportCsv(AuditLogFilterRequest filter, HttpServletResponse response) {
        validateDateRange(filter);
        List<AuditLog> logs = auditLogRepository.findByFiltersNoPaging(
                filter.getUserId(),
                normalize(filter.getAction()),
                normalize(filter.getEntityType()),
                filter.getFromDate(),
                filter.getToDate()
        );

        String filename = "audit_logs_" + LocalDateTime.now().format(CSV_FILENAME_FORMATTER) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try {
            PrintWriter writer = response.getWriter();
            writer.println("id,userId,action,entityType,entityId,ipAddress,createdAt");
            for (AuditLog log : logs) {
                writer.println(String.join(",",
                        csvValue(log.getId()),
                        csvValue(log.getUserId()),
                        csvValue(log.getAction()),
                        csvValue(log.getEntityType()),
                        csvValue(log.getEntityId()),
                        csvValue(log.getIpAddress()),
                        csvValue(log.getCreatedAt())
                ));
            }
            writer.flush();
        } catch (IOException ex) {
            throw new BusinessException("Không thể export audit log CSV");
        }
    }

    private void validateDateRange(AuditLogFilterRequest filter) {
        boolean hasFromDate = filter.getFromDate() != null;
        boolean hasToDate = filter.getToDate() != null;
        if (hasFromDate != hasToDate) {
            throw new BusinessException("Phải nhập cả fromDate và toDate");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getIpAddress(),
                auditLog.getCreatedAt()
        );
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\n") || text.contains("\r") || text.contains("\"")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
