package com.hospital.util;

import com.hospital.dto.request.DepartmentRequest;
import com.hospital.dto.response.DepartmentResponse;
import com.hospital.entity.Department;

public class DepartmentMapper {
    public static Department toEntity(DepartmentRequest request) {
        Department department = new Department();
        updateEntity(request, department);
        return department;
    }

    public static DepartmentResponse toResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setDeptName(department.getDeptName());
        response.setLocation(department.getLocation());
        response.setPhone(department.getPhone());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());
        return response;
    }

    public static void updateEntity(DepartmentRequest request, Department department) {
        department.setDeptName(request.getDeptName());
        department.setLocation(request.getLocation());
        department.setLocation(request.getLocation());
    }
}
