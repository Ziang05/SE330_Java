package com.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for medicine catalog data returned to clients.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {

    private Long id;
    private String medicineName;
    private String genericName;
    private String category;
    private String unit;
    private BigDecimal unitPrice;
    private boolean insuranceCovered;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
