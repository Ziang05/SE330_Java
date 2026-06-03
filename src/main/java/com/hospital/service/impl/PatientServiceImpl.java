package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.PatientRequest;
import com.hospital.dto.response.PatientResponse;
import com.hospital.entity.Patient;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.PatientRepository;
import com.hospital.service.PatientService;
import com.hospital.util.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sample PatientService implementation with basic CRUD logic.
 */
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    @Transactional
    @Auditable(action = "CREATE", entityType = "Patient")
    public PatientResponse create(PatientRequest request) {
        validateUniqueCccd(request.getCccd(), null);
        Patient saved = patientRepository.save(PatientMapper.toEntity(request));
        return PatientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        return PatientMapper.toResponse(findPatient(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getAll() {
        return patientRepository.findAll().stream()
                .map(PatientMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE", entityType = "Patient")
    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = findPatient(id);
        validateUniqueCccd(request.getCccd(), id);
        PatientMapper.copyToEntity(request, patient);
        return PatientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    @Auditable(action = "DELETE", entityType = "Patient")
    public void delete(Long id) {
        Patient patient = findPatient(id);
        patientRepository.delete(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> searchByName(String keyword) {
        return patientRepository.findByFullNameContainingIgnoreCase(keyword).stream()
                .map(PatientMapper::toResponse)
                .toList();
    }

    private Patient findPatient(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }

    private void validateUniqueCccd(String cccd, Long currentPatientId) {
        if (cccd == null || cccd.isBlank()) {
            return;
        }
        patientRepository.findByCccd(cccd)
                .filter(existing -> !existing.getId().equals(currentPatientId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Patient already exists with CCCD: " + cccd);
                });
    }
}
