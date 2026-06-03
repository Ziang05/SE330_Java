package com.hospital.repository;

import com.hospital.entity.Invoice;
import com.hospital.entity.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Invoice entities.
 *
 * <p>Chứa các query phục vụ:
 * <ul>
 *   <li>T39 – tìm hóa đơn theo bệnh nhân / trạng thái</li>
 *   <li>T42 – thống kê doanh thu theo ngày / tháng / năm (cho Reports)</li>
 * </ul>
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // ── T39: Tra cứu hóa đơn ────────────────────────────────────────────────────

    /**
     * Lấy hóa đơn theo bệnh nhân, sắp xếp mới nhất trước.
     */
    List<Invoice> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /**
     * Lấy hóa đơn theo trạng thái (PENDING / PAID / CANCELLED).
     */
    List<Invoice> findByStatus(InvoiceStatus status);

    /**
     * Lấy hóa đơn gắn với một MedicalRecord (quan hệ 1-1).
     */
    Optional<Invoice> findByMedicalRecordId(Long medicalRecordId);

    // ── T42: Báo cáo doanh thu ───────────────────────────────────────────────────

    /**
     * Tổng doanh thu (paid_amount) trong một khoảng thời gian.
     * Chỉ tính hóa đơn đã thanh toán (PAID).
     */
    @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM Invoice i " +
            "WHERE i.status = 'PAID' AND i.paidAt BETWEEN :from AND :to")
    BigDecimal sumRevenueBetween(@Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    /**
     * Đếm số lượt khám (hóa đơn đã thanh toán) trong khoảng thời gian.
     */
    @Query("SELECT COUNT(i) FROM Invoice i " +
            "WHERE i.status = 'PAID' AND i.paidAt BETWEEN :from AND :to")
    long countVisitsBetween(@Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    /**
     * T42 – Doanh thu gom nhóm theo ngày (dùng cho biểu đồ daily).
     * Trả về List[date_str, total_paid_amount, visit_count].
     */
    @Query("SELECT FUNCTION('DATE', i.paidAt) AS day, " +
            "       SUM(i.paidAmount)            AS revenue, " +
            "       COUNT(i)                     AS visits " +
            "FROM Invoice i " +
            "WHERE i.status = 'PAID' AND i.paidAt BETWEEN :from AND :to " +
            "GROUP BY FUNCTION('DATE', i.paidAt) " +
            "ORDER BY day ASC")
    List<Object[]> revenueGroupedByDay(@Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    /**
     * T42 – Tổng số tiền BHYT chi trả trong một khoảng thời gian.
     * Chỉ tính hóa đơn đã thanh toán (PAID).
     */
    @Query("SELECT COALESCE(SUM(i.insuranceAmount), 0) FROM Invoice i " +
            "WHERE i.status = 'PAID' AND i.paidAt BETWEEN :from AND :to")
    BigDecimal sumInsuranceBetween(@Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);
}

