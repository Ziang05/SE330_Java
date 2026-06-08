package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Static medicine catalog used for prescriptions and billing.
 *
 * <p>"Tĩnh" – không quản lý tồn kho hay hạn sử dụng (FIFO), chỉ lưu
 * thông tin tên, đơn vị, giá niêm yết và BHYT coverage.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "medicines")
public class Medicine extends BaseEntity {

    /**
     * Tên thương mại của thuốc.
     */
    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    /**
     * Tên hoạt chất / tên generic.
     */
    @Column(name = "generic_name", length = 150)
    private String genericName;

    /**
     * Nhóm thuốc (ví dụ: Kháng sinh, Giảm đau, Vitamin).
     */
    @Column(name = "category", length = 80)
    private String category;

    /**
     * Đơn vị tính (viên, ml, lọ, gói...).
     */
    @Column(name = "unit", length = 30)
    private String unit;

    /**
     * Giá bán lẻ một đơn vị (VNĐ).
     */
    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /**
     * Thuốc có nằm trong danh mục BHYT chi trả không.
     * true  → BHYT áp dụng
     * false → bệnh nhân tự trả 100%
     */
    @Column(name = "insurance_covered", nullable = false)
    private boolean insuranceCovered = false;

    /**
     * Có đang kinh doanh / còn hiệu lực trong danh mục không.
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;
}
