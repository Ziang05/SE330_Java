package com.hospital.dto.response;

import com.hospital.entity.enums.AppointmentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO đóng gói dữ liệu lịch hẹn trả về cho Frontend hiển thị.
 */
@Getter
@Setter
@NoArgsConstructor
public class AppointmentResponse {
    
    private Long id;
    private LocalDateTime apptDatetime;
    private AppointmentStatus status;
    private String representativeFullname;
    private String representativePhone;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long patientId;
    private String patientName;
    private String patientPhone;

    private Long doctorId;
    private String doctorName;
    private String departmentName;
}
