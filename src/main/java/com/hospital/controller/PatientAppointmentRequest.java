package com.hospital.controller;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PatientAppointmentRequest {
    @NotNull(message = "ID bác sĩ không được để trống")
    private Long doctorId;

    @NotNull(message = "Thời gian hẹn khám không được để trống")
    @FutureOrPresent(message = "Thời gian hẹn khám phải ở hiện tại hoặc tương lai")
    private LocalDateTime apptDatetime;

    @Nullable
    private String email;

    @Nullable
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String representativeFullname;

    @Nullable
    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String representativePhone;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String notes;
}
