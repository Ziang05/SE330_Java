package com.hospital.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientAppointmentRequest {

    @NotNull(message = "Vui lòng chọn bác sĩ khám")
    private Long doctorId;

    @NotNull(message = "Vui lòng chọn thời gian khám hẹn trước")
    @Future(message = "Thời gian hẹn khám phải nằm trong tương lai")
    private LocalDateTime appointmentDate;

    @NotBlank(message = "Vui lòng nhập lý do hoặc triệu chứng bệnh")
    private String reason;
}
