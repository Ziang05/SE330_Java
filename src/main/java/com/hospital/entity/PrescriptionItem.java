package com.hospital.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One medicine line inside a prescription.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "prescription_items")
public class PrescriptionItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "dosage", length = 120)
    private String dosage;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    /**
     * Giá tại thời điểm kê đơn (snapshot từ Medicine.unitPrice).
     * Không lấy trực tiếp từ Medicine.unitPrice khi xuất hóa đơn vì giá có thể thay đổi về sau.
     */
    @Column(name = "unit_price_at_time", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPriceAtTime = BigDecimal.ZERO;
}
