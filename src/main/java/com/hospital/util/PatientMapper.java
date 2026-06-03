package com.hospital.util;

import com.hospital.dto.request.PatientRequest;
import com.hospital.dto.response.PatientResponse;
import com.hospital.entity.Patient;

/**
 * Small mapper for the sample Patient module.
 */
public final class PatientMapper {

    private PatientMapper() {
    }

    public static Patient toEntity(PatientRequest request) {
        Patient patient = new Patient();
        copyToEntity(request, patient);
        return patient;
    }

    public static void copyToEntity(PatientRequest request, Patient patient) {
        patient.setFullName(request.getFullName());
        patient.setDob(request.getDob());
        patient.setGender(request.getGender());
        patient.setCccd(request.getCccd());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        patient.setBloodType(request.getBloodType());
        patient.setInsuranceNumber(request.getInsuranceNumber());
    }

    public static PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getDob(),
                patient.getGender(),
                patient.getCccd(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getBloodType(),
                patient.getInsuranceNumber(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
}
