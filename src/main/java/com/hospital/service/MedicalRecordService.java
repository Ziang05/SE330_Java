package com.hospital.service;

import com.hospital.dto.request.CheckInRequest;
import com.hospital.dto.response.MedicalRecordResponse;


public interface MedicalRecordService {
    MedicalRecordResponse checkInAndCreateRecord(CheckInRequest request);
    java.util.List<MedicalRecordResponse> getMedicalRecordsByPatientId(Long patientId);
    MedicalRecordResponse getById(Long id);
}
