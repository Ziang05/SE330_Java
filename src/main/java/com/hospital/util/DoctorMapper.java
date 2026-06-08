package com.hospital.util;

import com.hospital.dto.request.DoctorRequest;
import com.hospital.dto.response.DoctorResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;

public class DoctorMapper {
    public static Doctor toEntity(DoctorRequest request, Department department) {
        Doctor doctor = new Doctor();
        updateEntity(request, department, doctor);
        return doctor;
    }

    public static void updateEntity(DoctorRequest request, Department department, Doctor doctor) {
        doctor.setFullName(request.getFullName());
        doctor.setPhone(request.getPhone());
        doctor.setEmail(request.getEmail());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setHireDate(request.getHireDate());
        doctor.setDepartment(department);
    }

    public static DoctorResponse toResponse(Doctor saved) {
        DoctorResponse response = new DoctorResponse();
        response.setId(saved.getId());
        response.setFullName(saved.getFullName());
        response.setPhone(saved.getPhone());
        response.setEmail(saved.getEmail());
        response.setLicenseNumber(saved.getLicenseNumber());
        response.setHireDate(saved.getHireDate());
        response.setDepartmentId(saved.getDepartment().getId());
        response.setCreatedAt(saved.getCreatedAt());
        response.setUpdatedAt(saved.getUpdatedAt());
        return response;
    }
}
