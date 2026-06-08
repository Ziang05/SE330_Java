package com.hospital.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.entity.AuditLog;
import com.hospital.repository.AuditLogRepository;
import com.hospital.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Writes audit rows after methods annotated with @Auditable complete successfully.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAfterReturning(Auditable auditable, Object result) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(getUserIdFromJwt());
        auditLog.setAction(auditable.action());
        auditLog.setEntityType(resolveEntityType(auditable, result));
        auditLog.setEntityId(resolveEntityId(result));
        auditLog.setNewValue(toJson(result));
        auditLog.setIpAddress(resolveClientIp());
        auditLogRepository.save(auditLog);
    }

    private Long getUserIdFromJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }

    private String resolveEntityType(Auditable auditable, Object result) {
        if (!auditable.entityType().isBlank()) {
            return auditable.entityType();
        }
        return result == null ? null : result.getClass().getSimpleName();
    }

    private Long resolveEntityId(Object result) {
        if (result == null) {
            return null;
        }
        try {
            Method getId = result.getClass().getMethod("getId");
            Object value = getId.invoke(result);
            return value instanceof Long id ? id : null;
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private String toJson(Object result) {
        if (result == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            return String.valueOf(result);
        }
    }

    private String resolveClientIp() {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
