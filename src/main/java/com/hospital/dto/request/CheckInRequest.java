package com.hospital.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckInRequest {

    @NotNull(message = "ID của lịch hẹn không được để trống")
    private Long appointmentId;

    @Size(max = 1000, message = "Ghi chú lâm sàng ban đầu không được vượt quá 1000 ký tự")
    private String notes; // Ví dụ: Ghi nhận nhanh tình trạng lúc đến quầy (sốt cao, ho,...)
}
