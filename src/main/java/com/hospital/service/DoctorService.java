package com.hospital.service;

import com.hospital.dto.request.DoctorRequest;
import com.hospital.dto.response.DoctorResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface DoctorService {

    DoctorResponse create(@Valid DoctorRequest request);

    DoctorResponse update(Long doctorId, @Valid DoctorRequest request);

    void delete(Long doctorId);

    DoctorResponse get(Long doctorId);

    List<DoctorResponse> getAll();
}
