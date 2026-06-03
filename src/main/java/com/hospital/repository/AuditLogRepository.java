package com.hospital.repository;

import com.hospital.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository used by audit logging AOP.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            select al from AuditLog al
            where (:userId is null or al.userId = :userId)
              and (:action is null or lower(al.action) like lower(concat('%', :action, '%')))
              and (:entityType is null or al.entityType = :entityType)
              and (
                    :fromDate is null
                    or :toDate is null
                    or al.createdAt between :fromDate and :toDate
              )
            order by al.createdAt desc
            """)
    Page<AuditLog> findByFilters(
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("""
            select al from AuditLog al
            where (:userId is null or al.userId = :userId)
              and (:action is null or lower(al.action) like lower(concat('%', :action, '%')))
              and (:entityType is null or al.entityType = :entityType)
              and (
                    :fromDate is null
                    or :toDate is null
                    or al.createdAt between :fromDate and :toDate
              )
            order by al.createdAt desc
            """)
    List<AuditLog> findByFiltersNoPaging(
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
