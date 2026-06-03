package com.hospital.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DoctorResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String licenseNumber;
    private LocalDate hireDate;
    private Long departmentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
