package com.hospital.service.impl;

import com.hospital.dto.request.PrescriptionRequest;
import com.hospital.dto.request.PrescriptionItemRequest;
import com.hospital.dto.response.PrescriptionResponse;
import com.hospital.dto.response.PrescriptionItemResponse;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Medicine;
import com.hospital.entity.Prescription;
import com.hospital.entity.PrescriptionItem;
import com.hospital.entity.enums.PrescriptionStatus;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.MedicalRecordRepository;
import com.hospital.repository.MedicineRepository;
import com.hospital.repository.PrescriptionRepository;
import com.hospital.repository.PrescriptionItemRepository;
import com.hospital.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;

    @Override
    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionRequest request) {
        log.info("Bắt đầu khởi tạo đơn thuốc cho Hồ sơ khám ID: {}", request.getMedicalRecordId());

        MedicalRecord medicalRecord = medicalRecordRepository.findById(request.getMedicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", request.getMedicalRecordId()));

        Prescription prescription = new Prescription();
        prescription.setMedicalRecord(medicalRecord);
        prescription.setDoctor(medicalRecord.getDoctor());
        prescription.setIssuedDate(LocalDate.now());
        prescription.setStatus(PrescriptionStatus.DRAFT);

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        log.info("Đã lưu đơn thuốc tổng thành công. Prescription ID: {}", savedPrescription.getId());

        List<PrescriptionItem> savedItems = new ArrayList<>();

        for (PrescriptionItemRequest itemRequest : request.getItems()) {
            Medicine medicine = medicineRepository.findById(itemRequest.getMedicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", itemRequest.getMedicationId()));

            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(savedPrescription);
            item.setMedicine(medicine);
            item.setQuantity(itemRequest.getQuantity());
            item.setDosage(itemRequest.getDosage());
            item.setInstructions(request.getDoctorNotes());
            
            if (medicine.getUnitPrice() != null) {
                item.setUnitPriceAtTime(medicine.getUnitPrice());
            }

            PrescriptionItem savedItem = prescriptionItemRepository.save(item);
            savedItems.add(savedItem);
        }

        return mapToResponse(savedPrescription, savedItems);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long id) {
        log.info("Tìm kiếm chi tiết đơn thuốc ID: {}", id);
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        
        List<PrescriptionItem> items = prescriptionItemRepository.findByPrescriptionId(id);
        
        return mapToResponse(prescription, items);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<PrescriptionResponse> getPrescriptionsByPatientId(Long patientId) {
        log.info("Lay danh sach don thuoc cua Patient ID: {}", patientId);
        java.util.List<Prescription> prescriptions = prescriptionRepository.findByMedicalRecordPatientIdOrderByCreatedAtDesc(patientId);
        return prescriptions.stream()
                .map(p -> mapToResponse(p, prescriptionItemRepository.findByPrescriptionId(p.getId())))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Ham tien ich phang hoa du lieu Don thuoc sang DTO.
     */
    private PrescriptionResponse mapToResponse(Prescription prescription, List<PrescriptionItem> items) {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setId(prescription.getId());
        response.setMedicalRecordId(prescription.getMedicalRecord().getId());
        response.setCreatedAt(prescription.getCreatedAt());

        if (prescription.getMedicalRecord() != null) {
            response.setDoctorNotes(prescription.getMedicalRecord().getNotes());
            
            // Trích xuất thông tin Bệnh nhân an toàn từ MedicalRecord liên kết
            if (prescription.getMedicalRecord().getPatient() != null) {
                response.setPatientId(prescription.getMedicalRecord().getPatient().getId());
                response.setPatientName(prescription.getMedicalRecord().getPatient().getFullName());
            }
        }

        // Trích xuất thông tin Bác sĩ
        if (prescription.getDoctor() != null) {
            response.setDoctorId(prescription.getDoctor().getId());
            response.setDoctorName(prescription.getDoctor().getFullName());
        }

        // Ánh xạ danh sách dòng thuốc chi tiết sang DTO tương ứng
        List<PrescriptionItemResponse> itemResponses = new ArrayList<>();
        if (items != null) {
            for (PrescriptionItem item : items) {
                PrescriptionItemResponse itemDto = new PrescriptionItemResponse();
                itemDto.setId(item.getId());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setDosage(item.getDosage());
                
                if (item.getMedicine() != null) {
                    itemDto.setMedicationId(item.getMedicine().getId());
                    itemDto.setMedicationName(item.getMedicine().getMedicineName());
                    itemDto.setUnit(item.getMedicine().getUnit());
                }
                itemResponses.add(itemDto);
            }
        }
        response.setItems(itemResponses);

        return response;
    }
}
