package com.hospital.service.impl;

import com.hospital.dto.request.DoctorRequest;
import com.hospital.dto.response.DoctorResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.service.DoctorService;
import com.hospital.util.DoctorMapper;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository, DepartmentRepository departmentRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public DoctorResponse create(DoctorRequest request) {
        Department department = getDepartment(request);
        Doctor saved = this.doctorRepository.save(DoctorMapper.toEntity(request, department));
        return DoctorMapper.toResponse(saved);
    }

    @Nonnull
    private Department getDepartment(DoctorRequest request) {
        return this.departmentRepository.getReferenceById(request.getDepartmentId());
    }

    @Override
    public DoctorResponse update(Long doctorId, DoctorRequest request) {
        Doctor doctor = getDoctor(doctorId);
        Department department = getDepartment(request);
        DoctorMapper.updateEntity(request, department, doctor);
        return DoctorMapper.toResponse(this.doctorRepository.save(doctor));
    }

    @Override
    public void delete(Long doctorId) {
        Doctor doctor = getDoctor(doctorId);
        this.doctorRepository.delete(doctor);
    }

    @Nonnull
    private Doctor getDoctor(Long doctorId) {
        return this.doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
    }

    @Override
    public DoctorResponse get(Long doctorId) {
        return DoctorMapper.toResponse(getDoctor(doctorId));
    }

    @Override
    public List<DoctorResponse> getAll() {
        return this.doctorRepository.findAll().stream()
                .map(DoctorMapper::toResponse)
                .toList();
    }
}
