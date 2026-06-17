package com.hospital.dto.response;

import com.hospital.entity.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response returned after a successful patient self-registration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPatientResponse {

    private Long userId;
    private String username;
    private String email;

    // Patient profile
    private Long patientId;
    private String fullName;
    private LocalDate dob;
    private Gender gender;
    private String cccd;
    private String phone;
    private String address;
    private String bloodType;
    private String insuranceNumber;

    private LocalDateTime createdAt;
}
