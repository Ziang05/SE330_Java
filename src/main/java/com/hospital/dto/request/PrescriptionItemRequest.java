package com.hospital.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa chi tiết từng loại thuốc được kê trong đơn.
 */
@Getter
@Setter
@NoArgsConstructor
public class PrescriptionItemRequest {

    @NotNull(message = "ID của thuốc không được để trống")
    private Long medicationId;

    @NotNull(message = "Số lượng thuốc không được để trống")
    @Min(value = 1, message = "Số lượng thuốc phải lớn hơn hoặc bằng 1 viên/chai")
    private Integer quantity;

    @NotBlank(message = "Liều lượng và cách dùng không được để trống")
    private String dosage;
}
