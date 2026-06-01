package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.MedicineRequest;
import com.hospital.dto.response.MedicineResponse;
import com.hospital.entity.Medicine;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.MedicineRepository;
import com.hospital.service.MedicineService;
import com.hospital.util.MedicineMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementation of MedicineService – CRUD logic for the medicine catalog (T41). */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    // ── CREATE ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = "CREATE", entityType = "Medicine")
    public MedicineResponse create(MedicineRequest request) {
        // Kiểm tra trùng tên thuốc trước khi lưu
        if (medicineRepository.existsByMedicineNameIgnoreCase(request.getMedicineName())) {
            throw new DuplicateResourceException("Thuốc đã tồn tại trong danh mục: " + request.getMedicineName());
        }
        Medicine saved = medicineRepository.save(MedicineMapper.toEntity(request));
        log.info("Medicine created: id={}, name={}", saved.getId(), saved.getMedicineName());
        return MedicineMapper.toResponse(saved);
    }

    // ── READ ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public MedicineResponse getById(Long id) {
        return MedicineMapper.toResponse(findMedicine(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getAll() {
        return medicineRepository.findByActiveTrue().stream()
                .map(MedicineMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> searchByName(String keyword) {
        return medicineRepository.findByMedicineNameContainingIgnoreCase(keyword).stream()
                .map(MedicineMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getByCategory(String category) {
        return medicineRepository.findByCategory(category).stream()
                .map(MedicineMapper::toResponse)
                .toList();
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = "UPDATE", entityType = "Medicine")
    public MedicineResponse update(Long id, MedicineRequest request) {
        Medicine medicine = findMedicine(id);

        // Cho phép đổi tên, nhưng kiểm tra tên mới không trùng với thuốc KHÁC
        boolean nameChanged = !medicine.getMedicineName().equalsIgnoreCase(request.getMedicineName());
        if (nameChanged && medicineRepository.existsByMedicineNameIgnoreCase(request.getMedicineName())) {
            throw new DuplicateResourceException("Thuốc đã tồn tại trong danh mục: " + request.getMedicineName());
        }

        MedicineMapper.copyToEntity(request, medicine);
        Medicine updated = medicineRepository.save(medicine);
        log.info("Medicine updated: id={}, name={}", updated.getId(), updated.getMedicineName());
        return MedicineMapper.toResponse(updated);
    }

    // ── DELETE (soft) ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = "DELETE", entityType = "Medicine")
    public void delete(Long id) {
        Medicine medicine = findMedicine(id);
        medicine.setActive(false);          // Soft delete: chỉ đánh dấu không còn hiệu lực
        medicineRepository.save(medicine);
        log.info("Medicine soft-deleted: id={}, name={}", id, medicine.getMedicineName());
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────────

    private Medicine findMedicine(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", id));
    }
}
