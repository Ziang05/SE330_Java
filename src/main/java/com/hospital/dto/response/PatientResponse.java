package com.hospital.dto.response;

import com.hospital.entity.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for patient data returned to clients.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private Long id;
    private String fullName;
    private LocalDate dob;
    private Gender gender;
    private String cccd;
    private String phone;
    private String address;
    private String bloodType;
    private String insuranceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
