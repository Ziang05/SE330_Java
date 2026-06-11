package com.hospital.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO đóng gói dữ liệu Hồ sơ khám bệnh được sinh ra sau khi tiếp nhận thành công.
 */
@Getter
@Setter
@NoArgsConstructor
public class MedicalRecordResponse {
    
    private Long id;
    private Long appointmentId;
    private LocalDate visitDate;
    
    private String status;        
    
    private Long patientId;
    private String patientName;
    private String patientPhone;

    private Long doctorId;
    private String doctorName;
    private String departmentName;
    
    private LocalDateTime createdAt;
}
