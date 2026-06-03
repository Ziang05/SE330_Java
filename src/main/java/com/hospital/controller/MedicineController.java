package com.hospital.controller;

import com.hospital.dto.request.MedicineRequest;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.MedicineResponse;
import com.hospital.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the medicine catalog (T41).
 *
 * <p>Endpoints:
 * <pre>
 *   GET    /api/v1/medicines              – lấy danh sách thuốc đang hoạt động
 *   GET    /api/v1/medicines/{id}         – lấy chi tiết một thuốc
 *   GET    /api/v1/medicines/search       – tìm thuốc theo tên (?keyword=...)
 *   GET    /api/v1/medicines/category     – lọc theo nhóm thuốc (?category=...)
 *   POST   /api/v1/medicines              – thêm thuốc mới (ADMIN)
 *   PUT    /api/v1/medicines/{id}         – cập nhật thuốc (ADMIN)
 *   DELETE /api/v1/medicines/{id}         – xóa mềm thuốc (ADMIN)
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    // ── READ (mở cho staff khám bệnh tra cứu) ────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<MedicineResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.getById(id)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.searchByName(keyword)));
    }

    @GetMapping("/category")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getByCategory(@RequestParam String category) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.getByCategory(category)));
    }

    // ── WRITE (chỉ ADMIN quản lý danh mục) ───────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MedicineResponse>> create(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Thêm thuốc thành công", medicineService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thuốc thành công", medicineService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thuốc thành công", null));
    }
}
