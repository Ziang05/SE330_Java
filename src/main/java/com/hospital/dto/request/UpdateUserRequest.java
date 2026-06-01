package com.hospital.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRequest {

    @Email(message = "Email must be valid")
    private String email;

    private String fullName;

    private List<String> roleNames;

    private Long doctorId;
}
