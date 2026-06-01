package com.hospital.service;

import com.hospital.dto.request.CreateUserRequest;
import com.hospital.dto.request.ResetPasswordRequest;
import com.hospital.dto.request.UpdateUserRequest;
import com.hospital.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    Page<UserResponse> getAllUsers(String keyword, Boolean isActive, Pageable pageable);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void toggleActive(Long id);

    void resetPassword(Long id, ResetPasswordRequest request);
}
