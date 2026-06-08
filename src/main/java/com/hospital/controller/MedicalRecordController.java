package com.hospital.controller;

import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.request.CheckInRequest;
import com.hospital.dto.response.MedicalRecordResponse;
import com.hospital.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller quản lý các Endpoint liên quan đến Hồ sơ khám bệnh và Tiếp nhận (Check-in).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * API Thực hiện thủ tục Tiếp nhận bệnh nhân tại quầy (Check-in).
     * Quyền truy cập (Đầu việc 6): Chỉ ADMIN hoặc NURSE mới có quyền tiếp đón bệnh nhân.
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ApiResponse<MedicalRecordResponse> checkInPatient(@Valid @RequestBody CheckInRequest request) {
        log.info("REST request - Tiếp nhận bệnh nhân cho Appointment ID: {}", request.getAppointmentId());
        
        MedicalRecordResponse response = medicalRecordService.checkInAndCreateRecord(request);
        
        return ApiResponse.ok("Tiếp nhận bệnh nhân tại quầy thành công! Đã khởi tạo hồ sơ và gán phòng khám.", response);
    }
}
