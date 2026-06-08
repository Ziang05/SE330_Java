package com.hospital.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DepartmentResponse {
    private Long id;
    private String deptName;
    private String location;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
