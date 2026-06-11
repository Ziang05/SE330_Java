package com.hospital.controller;

import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentResponse;
import com.hospital.entity.enums.AppointmentStatus;
import com.hospital.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * REST Controller quản lý các Endpoint liên quan đến Lịch khám bệnh (Appointment).
 * Đã sửa lỗi Undefined method bằng cách chuyển sang sử dụng hàm ApiResponse.ok(...)
 */
@Slf4j
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * API Đặt lịch hẹn khám bệnh mới.
     * Quyền truy cập: ADMIN hoặc NURSE.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ApiResponse<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        log.info("REST request - Tạo lịch hẹn mới cho Patient ID: {}", request.getPatientId());
        AppointmentResponse response = appointmentService.createAppointment(request);
        
        return ApiResponse.ok("Đặt lịch khám bệnh thành công!", response);
    }

    /**
     * API Lấy thông tin chi tiết một lịch hẹn theo ID.
     * Quyền truy cập: ADMIN, DOCTOR, NURSE.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ApiResponse<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        log.info("REST request - Lấy chi tiết lịch hẹn ID: {}", id);
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        
        return ApiResponse.ok("Tải thông tin lịch hẹn thành công.", response);
    }

    /**
     * API Lấy toàn bộ danh sách lịch hẹn trong hệ thống.
     * Quyền truy cập: ADMIN hoặc NURSE.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ApiResponse<List<AppointmentResponse>> getAllAppointments() {
        log.info("REST request - Lấy toàn bộ danh sách lịch hẹn");
        List<AppointmentResponse> response = appointmentService.getAllAppointments();
        
        return ApiResponse.ok("Tải danh sách lịch hẹn thành công.", response);
    }

    /**
     * API Lấy danh sách lịch hẹn của một Bệnh nhân cụ thể.
     * Quyền truy cập: ADMIN, DOCTOR, NURSE.
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ApiResponse<List<AppointmentResponse>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        log.info("REST request - Lấy danh sách lịch hẹn của Patient ID: {}", patientId);
        List<AppointmentResponse> response = appointmentService.getAppointmentsByPatientId(patientId);
        
        return ApiResponse.ok("Tải danh sách lịch hẹn của bệnh nhân thành công.", response);
    }

    /**
     * API Cập nhật trạng thái lịch hẹn (Xác nhận, Hủy, hoặc Tiếp nhận CHECKED_IN).
     * Quyền truy cập: ADMIN hoặc NURSE.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ApiResponse<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        log.info("REST request - Cập nhật trạng thái lịch hẹn ID: {} sang {}", id, status);
        AppointmentResponse response = appointmentService.updateAppointmentStatus(id, status);
        
        String message = "Cập nhật trạng thái lịch hẹn thành công.";
        if (status == AppointmentStatus.CHECKED_IN) {
            message = "Tiếp nhận bệnh nhân vào phòng đợi thành công (Checked-in)!";
        }
        
        return ApiResponse.ok(message, response);
    }

    /**
    * API Kiểm tra trùng lịch khám của bác sĩ nhanh.
    * Dùng cho Frontend check real-time khi bệnh nhân vừa chọn giờ trên lịch.
    */
    @GetMapping("/check-conflict")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ApiResponse<Boolean> checkConflict(
            @RequestParam Long doctorId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime apptDatetime) {
        
        log.info("REST request - Kiểm tra trùng lịch cho Doctor ID: {} lúc: {}", doctorId, apptDatetime);
        boolean isConflicted = appointmentService.isDoctorConflicted(doctorId, apptDatetime);
        
        String message = isConflicted ? "Khung giờ này đã có người đặt!" : "Khung giờ này hoàn toàn trống.";
        return ApiResponse.ok(message, isConflicted);
    }

    @GetMapping("/{id}/export-pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<InputStreamResource> exportAppointmentSlip(@PathVariable Long id) {
        log.info("REST request - Yêu cầu in/xuất PDF cho Lịch hẹn ID: {}", id);
        
        // Gọi tầng nghiệp vụ lấy luồng dữ liệu thô
        ByteArrayInputStream pdfStream = appointmentService.exportAppointmentSlipPdf(id);
        
        // Đóng gói mảng byte vào Resource của Spring
        InputStreamResource resource = new InputStreamResource(pdfStream);
        
        // Thiết lập cấu trúc Header HTTP chuẩn cho tệp tin truyền thông trực tuyến
        HttpHeaders headers = new HttpHeaders();
        // "inline": Mở tab preview trên trình duyệt; "filename": Tên file khi tải về
        headers.add("Content-Disposition", "inline; filename=Appointment_Slip_LH" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF) // Khai báo định dạng file trả về là PDF
                .body(resource);
    }
}
