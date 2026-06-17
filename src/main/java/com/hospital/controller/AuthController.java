package com.hospital.controller;

import com.hospital.config.JwtProperties;
import com.hospital.dto.request.LoginRequest;
import com.hospital.dto.request.RefreshTokenRequest;
import com.hospital.dto.request.RegisterPatientRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.LoginResponse;
import com.hospital.dto.response.RegisterPatientResponse;
import com.hospital.security.JwtTokenProvider;
import com.hospital.security.UserPrincipal;
import com.hospital.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(buildLoginResponse(principal)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token không hợp lệ hoặc đã hết hạn"));
        }

        Claims claims = jwtTokenProvider.getClaimsFromToken(refreshToken);
        if (claims.containsKey("userId") || claims.containsKey("roles")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token không hợp lệ hoặc đã hết hạn"));
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.ok(buildLoginResponse(principal, refreshToken)));
    }

    @PostMapping("/register-patient")
    public ResponseEntity<ApiResponse<RegisterPatientResponse>> registerPatient(
            @Valid @RequestBody RegisterPatientRequest request) {
        RegisterPatientResponse response = authService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đăng ký tài khoản bệnh nhân thành công", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công. Vui lòng xóa token phía client.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> userInfo = Map.of(
                "userId", principal.getId(),
                "username", principal.getUsername(),
                "roles", principal.getRoles()
        );
        return ResponseEntity.ok(ApiResponse.ok(userInfo));
    }

    private LoginResponse buildLoginResponse(UserPrincipal principal) {
        return buildLoginResponse(principal, jwtTokenProvider.generateRefreshToken(principal));
    }

    private LoginResponse buildLoginResponse(UserPrincipal principal, String refreshToken) {
        return new LoginResponse(
                jwtTokenProvider.generateAccessToken(principal),
                refreshToken,
                "Bearer",
                jwtProperties.getAccessTokenExpiration(),
                principal.getId(),
                principal.getUsername(),
                principal.getRoles()
        );
    }
}
