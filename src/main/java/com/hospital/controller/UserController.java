package com.hospital.controller;

import com.hospital.dto.request.CreateUserRequest;
import com.hospital.dto.request.ResetPasswordRequest;
import com.hospital.dto.request.UpdateUserRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.UserResponse;
import com.hospital.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get users with optional keyword and status filters")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers(keyword, isActive, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    @PostMapping
    @Operation(summary = "Create user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User created successfully", userService.createUser(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateUser(id, request)));
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle user active status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(@PathVariable Long id) {
        userService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    @PatchMapping("/{id}/reset-password")
    @Operation(summary = "Reset user password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Reset mật khẩu thành công", null));
    }
}
