package com.hospital.repository;

import com.hospital.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository used by audit logging AOP. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
