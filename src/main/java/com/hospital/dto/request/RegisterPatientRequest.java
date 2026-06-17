package com.hospital.dto.request;

import com.hospital.entity.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Registration payload combining account credentials and patient identity.
 */
@Getter
@Setter
@NoArgsConstructor
public class RegisterPatientRequest {

    // Account credentials
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    // Patient identity – used to create or link a Patient record
    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @PastOrPresent(message = "Date of birth must not be in the future")
    private LocalDate dob;

    private Gender gender;

    @Size(max = 20, message = "CCCD must be at most 20 characters")
    private String cccd;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Size(max = 10, message = "Blood type must be at most 10 characters")
    private String bloodType;

    @Size(max = 50, message = "Insurance number must be at most 50 characters")
    private String insuranceNumber;
}
