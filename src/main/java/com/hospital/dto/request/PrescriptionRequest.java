package com.hospital.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO tổng toàn bộ thông tin đơn thuốc do Bác sĩ kê.
 */
@Getter
@Setter
@NoArgsConstructor
public class PrescriptionRequest {

    @NotNull(message = "ID hồ sơ khám bệnh không được để trống")
    private Long medicalRecordId;

    private String doctorNotes; // Lời dặn dò tổng quát của Bác sĩ (ví dụ: "Tái khám sau 7 ngày")

    @Valid
    @NotEmpty(message = "Đơn thuốc phải có ít nhất một loại thuốc")
    private List<PrescriptionItemRequest> items;
}
