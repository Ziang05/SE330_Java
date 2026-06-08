package com.hospital.service;

import com.hospital.dto.request.DepartmentRequest;
import com.hospital.dto.response.DepartmentResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(@Valid DepartmentRequest request);

    DepartmentResponse update(Long departmentId, @Valid DepartmentRequest request);

    void delete(Long departmentId);

    DepartmentResponse get(Long departmentId);

    List<DepartmentResponse> getAll();
}
