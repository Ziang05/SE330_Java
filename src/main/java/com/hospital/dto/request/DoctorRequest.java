package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DoctorRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Size(max = 120, message = "Email must be at most 120 characters")
    private String email;

    @Size(max = 50, message = "License number must be at most 50 characters")
    private String licenseNumber;

    @PastOrPresent(message = "Hire date must not be in the future")
    private LocalDate hireDate;

    private Long departmentId;
}
