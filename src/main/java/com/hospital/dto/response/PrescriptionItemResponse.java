package com.hospital.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO phẳng hóa chi tiết dòng thuốc để trả về giao diện.
 */
@Getter
@Setter
@NoArgsConstructor
public class PrescriptionItemResponse {
    private Long id;
    private Long medicationId;
    private String medicationName;
    private String unit;           // Đơn vị tính: Viên, Chai, Gói...
    private Integer quantity;
    private String dosage;
}
