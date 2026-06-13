package com.hospital.controller;

import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.request.CheckInRequest;
import com.hospital.dto.response.MedicalRecordResponse;
import com.hospital.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller qu岷� l媒 c谩c Endpoint li锚n quan 膽岷縩 H峄� s啤 kh谩m b峄噉h v脿 Ti岷縫 nh岷璶 (Check-in).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * API Th峄眂 hi峄噉 th峄� t峄� Ti岷縫 nh岷璶 b峄噉h nh芒n t岷� qu岷� (Check-in).
     * Ch峄� ADMIN ho岷穋 NURSE m峄沬 c贸 quy峄乶 ti岷縫 膽贸n b峄噉h nh芒n.
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ApiResponse<MedicalRecordResponse> checkInPatient(@Valid @RequestBody CheckInRequest request) {
        log.info("REST request - Ti岷縫 nh岷璶 b峄噉h nh芒n cho Appointment ID: {}", request.getAppointmentId());
        
        MedicalRecordResponse response = medicalRecordService.checkInAndCreateRecord(request);
        
        return ApiResponse.ok("Ti岷縫 nh岷璶 b峄噉h nh芒n t岷� qu岷� th脿nh c么ng! 膼茫 kh峄焛 t岷� h峄� s啤 v脿 g谩n ph貌ng kh谩m.", response);
    }

    /**
     * API lay danh sach ho so kham cua mot benh nhan theo patient ID.
     * Dung de ho tro tim kiem Medical Record ID tren frontend.
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ApiResponse<java.util.List<MedicalRecordResponse>> getMedicalRecordsByPatientId(@PathVariable Long patientId) {
        log.info("REST request - Lay danh sach ho so kham cua Patient ID: {}", patientId);
        java.util.List<MedicalRecordResponse> response = medicalRecordService.getMedicalRecordsByPatientId(patientId);
        return ApiResponse.ok("Tai danh sach ho so kham thanh cong.", response);
    }
}