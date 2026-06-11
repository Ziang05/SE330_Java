package com.hospital.service;

import com.hospital.dto.request.LabTestCreateRequest;
import com.hospital.dto.request.LabTestResultRequest;
import com.hospital.dto.response.LabTestResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LabTestService {
    LabTestResponse createLabTest(LabTestCreateRequest request);
    LabTestResponse updateLabTestResult(Long id, LabTestResultRequest request, MultipartFile file);
    List<LabTestResponse> getPendingLabTests();
}
