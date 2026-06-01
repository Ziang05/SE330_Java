package com.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Boolean isActive;
    private List<String> roles;
    private Long doctorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
