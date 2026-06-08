package com.hospital.service;

import com.hospital.dto.request.PatientRequest;
import com.hospital.dto.response.PatientResponse;

import java.util.List;

/**
 * Sample service contract for patient CRUD.
 */
public interface PatientService {

    PatientResponse create(PatientRequest request);

    PatientResponse getById(Long id);

    List<PatientResponse> getAll();

    PatientResponse update(Long id, PatientRequest request);

    void delete(Long id);

    List<PatientResponse> searchByName(String keyword);
}
