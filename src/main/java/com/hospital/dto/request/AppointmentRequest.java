package com.hospital.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO hứng dữ liệu tạo mới hoặc cập nhật lịch hẹn từ Frontend.
 */
@Getter
@Setter
@NoArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "ID bệnh nhân không được để trống")
    private Long patientId;

    @NotNull(message = "ID bác sĩ không được để trống")
    private Long doctorId;

    @NotNull(message = "Thời gian hẹn khám không được để trống")
    @FutureOrPresent(message = "Thời gian hẹn khám phải ở hiện tại hoặc tương lai")
    private LocalDateTime apptDatetime;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String notes;
}
