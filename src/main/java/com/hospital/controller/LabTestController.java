package com.hospital.controller;

import com.hospital.dto.request.LabTestCreateRequest;
import com.hospital.dto.request.LabTestResultRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.LabTestResponse;
import com.hospital.service.LabTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller quản lý các chỉ định và kết quả Xét nghiệm (Lab Test).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/lab-tests")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    /**
     * API Bác sĩ ra chỉ định xét nghiệm cận lâm sàng.
     * Quyền truy cập: Chỉ ADMIN hoặc DOCTOR mới có quyền ra chỉ định.
     * URL: POST /api/v1/lab-tests
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ApiResponse<LabTestResponse> createLabTest(@Valid @RequestBody LabTestCreateRequest request) {
        log.info("REST request - Tạo chỉ định xét nghiệm: {} cho Hồ sơ ID: {}", 
                request.getTestName(), request.getMedicalRecordId());
        
        LabTestResponse response = labTestService.createLabTest(request);
        return ApiResponse.ok("Đã tạo phiếu chỉ định xét nghiệm thành công!", response);
    }

    /**
     * API Kỹ thuật viên cập nhật kết quả xét nghiệm và upload file đính kèm.
     * Lưu ý: Sử dụng Multipart/form-data để nhận đồng thời JSON và File.
     * Quyền truy cập: ADMIN hoặc NURSE (Kỹ thuật viên phòng máy).
     * URL: PUT /api/v1/lab-tests/{id}/result
     */
    @PutMapping(value = "/{id}/result", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ApiResponse<LabTestResponse> updateLabTestResult(
            @PathVariable Long id,
            @RequestPart("data") @Valid LabTestResultRequest request, // Nhận phần JSON kết quả
            @RequestPart(value = "file", required = false) MultipartFile file // Nhận phần File hình ảnh
    ) {
        log.info("REST request - Cập nhật kết quả cho Phiếu xét nghiệm ID: {}", id);
        
        LabTestResponse response = labTestService.updateLabTestResult(id, request, file);
        return ApiResponse.ok("Cập nhật kết quả xét nghiệm và tải file thành công!", response);
    }

    /**
     * API Lấy danh sách hàng đợi các xét nghiệm đang chờ thực hiện (ORDERED).
     * Phục vụ cho màn hình làm việc của Kỹ thuật viên/Y tá phòng xét nghiệm.
     * URL: GET /api/v1/lab-tests/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ApiResponse<List<LabTestResponse>> getPendingLabTests() {
        log.info("REST request - Lấy danh sách hàng đợi xét nghiệm");
        
        List<LabTestResponse> responses = labTestService.getPendingLabTests();
        return ApiResponse.ok("Tải danh sách hàng đợi thành công!", responses);
    }
}
