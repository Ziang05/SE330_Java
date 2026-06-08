package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentRequest {
    @NotBlank(message = "Department name is required")
    @Size(max = 120, message = "Department name must be at most 120 characters")
    private String deptName;

    @Size(max = 120, message = "Location must be at most 120 characters")
    private String location;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;
}
