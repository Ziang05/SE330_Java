package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.DepartmentRequest;
import com.hospital.dto.response.DepartmentResponse;
import com.hospital.entity.Department;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DepartmentRepository;
import com.hospital.service.DepartmentService;
import com.hospital.util.DepartmentMapper;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    @Auditable(action = "CREATE", entityType = "Department")
    public DepartmentResponse create(DepartmentRequest request) {
        Department department = this.departmentRepository.save(DepartmentMapper.toEntity(request));
        return DepartmentMapper.toResponse(department);
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE", entityType = "Department")
    public DepartmentResponse update(Long departmentId, DepartmentRequest request) {
        Department department = getDepartment(departmentId);
        DepartmentMapper.updateEntity(request, department);
        return DepartmentMapper.toResponse(this.departmentRepository.save(department));
    }

    @Nonnull
    private Department getDepartment(Long departmentId) {
        return this.departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
    }

    @Override
    @Transactional
    @Auditable(action = "DELETE", entityType = "Department")
    public void delete(Long departmentId) {
        Department department = getDepartment(departmentId);
        this.departmentRepository.delete(department);
    }

    @Override
    public DepartmentResponse get(Long departmentId) {
        return DepartmentMapper.toResponse(getDepartment(departmentId));
    }

    @Override
    public List<DepartmentResponse> getAll() {
        return this.departmentRepository.findAll().stream()
                .map(DepartmentMapper::toResponse)
                .toList();
    }
}
