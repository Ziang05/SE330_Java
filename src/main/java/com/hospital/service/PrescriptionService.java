package com.hospital.service;

import com.hospital.dto.request.PrescriptionRequest;
import com.hospital.dto.response.PrescriptionResponse;

public interface PrescriptionService {
    PrescriptionResponse createPrescription(PrescriptionRequest request);
    PrescriptionResponse getPrescriptionById(Long id);
}
