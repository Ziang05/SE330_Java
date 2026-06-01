package com.hospital.repository;

import com.hospital.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for the Medicine catalog.
 *
 * <p>Phục vụ:
 * <ul>
 *   <li>T41 – CRUD danh mục thuốc</li>
 *   <li>T39 – tính phí thuốc trong đơn kê</li>
 * </ul>
 */
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    /** Tìm thuốc theo tên (không phân biệt hoa thường). */
    List<Medicine> findByMedicineNameContainingIgnoreCase(String medicineName);

    /** Lọc theo nhóm thuốc. */
    List<Medicine> findByCategory(String category);

    /** Lấy danh sách thuốc đang hoạt động (active = true). */
    List<Medicine> findByActiveTrue();

    /** Lấy danh sách thuốc thuộc danh mục BHYT. */
    List<Medicine> findByInsuranceCoveredTrueAndActiveTrue();

    /** Kiểm tra tên thuốc đã tồn tại chưa (dùng khi thêm mới). */
    boolean existsByMedicineNameIgnoreCase(String medicineName);
}
