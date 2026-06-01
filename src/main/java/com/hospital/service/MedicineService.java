package com.hospital.service;

import com.hospital.dto.request.MedicineRequest;
import com.hospital.dto.response.MedicineResponse;

import java.util.List;

/** Business contract for medicine catalog management (T41). */
public interface MedicineService {

    /** Thêm một loại thuốc mới vào danh mục. */
    MedicineResponse create(MedicineRequest request);

    /** Lấy thông tin chi tiết của một loại thuốc theo ID. */
    MedicineResponse getById(Long id);

    /** Lấy toàn bộ danh sách thuốc đang hoạt động (active = true). */
    List<MedicineResponse> getAll();

    /** Cập nhật thông tin một loại thuốc. */
    MedicineResponse update(Long id, MedicineRequest request);

    /**
     * Xóa mềm (soft delete) một loại thuốc: đặt active = false.
     * Không xóa khỏi database để tránh phá vỡ dữ liệu hóa đơn cũ.
     */
    void delete(Long id);

    /** Tìm thuốc theo tên (không phân biệt hoa thường). */
    List<MedicineResponse> searchByName(String keyword);

    /** Lọc thuốc theo nhóm/danh mục. */
    List<MedicineResponse> getByCategory(String category);
}
