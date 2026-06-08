package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.LabTestCreateRequest;
import com.hospital.dto.request.LabTestResultRequest;
import com.hospital.dto.response.LabTestResponse;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.LabTest;
import com.hospital.entity.enums.LabTestStatus;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.MedicalRecordRepository;
import com.hospital.repository.LabTestRepository;
import com.hospital.service.LabTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabTestServiceImpl implements LabTestService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final LabTestRepository labTestRepository;

    private final String UPLOAD_DIR = "uploads/";

    @Override
    @Transactional
    @Auditable(action = "CREATE_LAB_TEST", entityType = "LabTest")
    public LabTestResponse createLabTest(LabTestCreateRequest request) {
        log.info("Bắt đầu tạo chỉ định xét nghiệm cho Hồ sơ khám ID: {}", request.getMedicalRecordId());

        MedicalRecord medicalRecord = medicalRecordRepository.findById(request.getMedicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", request.getMedicalRecordId()));

        LabTest labTest = new LabTest();
        labTest.setMedicalRecord(medicalRecord);
        labTest.setTestType(request.getTestName());
        labTest.setOrderedBy(medicalRecord.getDoctor());
        labTest.setStatus(LabTestStatus.ORDERED);

        LabTest savedTest = labTestRepository.save(labTest);
        log.info("Đã tạo phiếu xét nghiệm thành công. LabTest ID: {}", savedTest.getId());

        return mapToResponse(savedTest);
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE_LAB_TEST_RESULT", entityType = "LabTest")
    public LabTestResponse updateLabTestResult(Long id, LabTestResultRequest request, MultipartFile file) {
        log.info("Cập nhật kết quả xét nghiệm cho Phiếu ID: {}", id);

        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LabTest", "id", id));

        if (LabTestStatus.COMPLETED.equals(labTest.getStatus())) {
            throw new BusinessException("Phiếu xét nghiệm này đã hoàn thành và đóng gói kết quả, không thể chỉnh sửa!");
        }

        labTest.setResult(request.getResult());
        labTest.setTestDate(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            try {
                File directory = new File(UPLOAD_DIR);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String originalFileName = file.getOriginalFilename();
                String cleanFileName = originalFileName != null ? originalFileName.replaceAll("[^a-zA-Z0-9.]", "_") : "file";
                String storedFileName = UUID.randomUUID().toString() + "_" + cleanFileName;

                Path targetLocation = Paths.get(UPLOAD_DIR).resolve(storedFileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                log.info("Đã lưu file vật lý kết quả tại: {}", targetLocation.toString());

                String fileUrl = "/uploads/" + storedFileName;
                labTest.setResultFileUrl(fileUrl);

            } catch (IOException ex) {
                log.error("Lỗi khi lưu file kết quả xét nghiệm: {}", ex.getMessage());
                throw new BusinessException("Hệ thống gặp sự cố khi lưu trữ file hình ảnh, vui lòng thử lại sau!");
            }
        }

        labTest.setStatus(LabTestStatus.COMPLETED);
        LabTest updatedTest = labTestRepository.save(labTest);
        log.info("Đã cập nhật xong kết quả cho Phiếu xét nghiệm ID: {}", updatedTest.getId());

        return mapToResponse(updatedTest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestResponse> getPendingLabTests() {
        log.info("Lấy danh sách hàng đợi các phiếu xét nghiệm đang ở trạng thái ORDERED (Chờ thực hiện)");
        return labTestRepository.findByStatus(LabTestStatus.ORDERED).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Hàm tiện ích phẳng hóa thực thể Xét nghiệm sang DTO Response.
     */
    private LabTestResponse mapToResponse(LabTest labTest) {
        LabTestResponse response = new LabTestResponse();
        response.setId(labTest.getId());
        response.setMedicalRecordId(labTest.getMedicalRecord().getId());
        response.setTestName(labTest.getTestType()); // Ánh xạ từ testType sang testName cho Frontend hiển thị
        response.setResult(labTest.getResult());
        response.setResultFileUrl(labTest.getResultFileUrl());
        
        if (labTest.getStatus() != null) {
            response.setStatus(labTest.getStatus().name());
        }
        
        response.setCreatedAt(labTest.getCreatedAt());
        response.setUpdatedAt(labTest.getUpdatedAt());

        if (labTest.getOrderedBy() != null) {
            response.setDoctorName(labTest.getOrderedBy().getFullName());
        } else if (labTest.getMedicalRecord().getDoctor() != null) {
            response.setDoctorName(labTest.getMedicalRecord().getDoctor().getFullName());
        }

        if (labTest.getMedicalRecord() != null && labTest.getMedicalRecord().getPatient() != null) {
            response.setPatientId(labTest.getMedicalRecord().getPatient().getId());
            response.setPatientName(labTest.getMedicalRecord().getPatient().getFullName());
            response.setDescription(labTest.getMedicalRecord().getNotes());
        }

        return response;
    }
}
