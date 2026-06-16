package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO hứng dữ liệu khi Bác sĩ ra chỉ định Xét nghiệm cận lâm sàng.
 */
@Getter
@Setter
@NoArgsConstructor
public class LabTestCreateRequest {

    @NotNull(message = "ID hồ sơ khám bệnh không được để trống")
    private Long medicalRecordId;

    @NotBlank(message = "Tên loại xét nghiệm/chỉ định không được để trống")
    @Size(max = 150, message = "Tên loại xét nghiệm không được vượt quá 150 ký tự")
    private String testName;

    @Size(max = 500, message = "Yêu cầu chi tiết không được vượt quá 500 ký tự")
    private String description;

    @PositiveOrZero(message = "Phí xét nghiệm không được âm")
    private BigDecimal fee;
}
