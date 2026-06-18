package com.hospital.service;

import com.hospital.dto.request.PrescriptionRequest;
import com.hospital.dto.request.PrescriptionItemRequest;
import com.hospital.dto.response.PrescriptionResponse;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Medicine;
import com.hospital.entity.Prescription;
import com.hospital.entity.enums.PrescriptionStatus;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.MedicalRecordRepository;
import com.hospital.repository.MedicineRepository;
import com.hospital.repository.PrescriptionRepository;
import com.hospital.service.impl.PrescriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test bọc toàn bộ luồng xử lý Nghiệp vụ Đơn thuốc (Prescription Service).
 * Đã chuẩn hóa khớp 100% với các phương thức thực tế trong PrescriptionService.java.
 */
@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private PrescriptionRequest validRequest;
    private PrescriptionItemRequest itemRequest;
    private MedicalRecord mockRecord;
    private Medicine mockMedicine;
    private Prescription mockSavedPrescription;

    @BeforeEach
    void setUp() {
        // Chuẩn bị DTO item thuốc chi tiết
        itemRequest = new PrescriptionItemRequest();
        itemRequest.setMedicationId(50L);
        itemRequest.setQuantity(20);
        itemRequest.setDosage("Uống 2 viên/ngày");

        // Chuẩn bị DTO Request tạo đơn thuốc
        validRequest = new PrescriptionRequest();
        validRequest.setMedicalRecordId(100L);
        validRequest.setItems(Collections.singletonList(itemRequest));

        // Giả lập Hồ sơ bệnh án gốc (MedicalRecord)
        mockRecord = new MedicalRecord();
        mockRecord.setId(100L);
        
        // Giả lập Thuốc trong danh mục (Medicine)
        mockMedicine = new Medicine();
        mockMedicine.setId(50L);
        mockMedicine.setMedicineName("Paracetamol 500mg");

        // Khởi tạo thực thể chuẩn theo cấu trúc Entity nhóm thiết kế
        mockSavedPrescription = new Prescription();
        mockSavedPrescription.setId(888L); 
        mockSavedPrescription.setMedicalRecord(mockRecord);
        mockSavedPrescription.setIssuedDate(LocalDate.now());
        mockSavedPrescription.setStatus(PrescriptionStatus.DRAFT);
    }

    @Test
    @DisplayName("Kịch bản 1.1: Kê đơn thuốc thành công - Dữ liệu hợp lệ")
    void createPrescription_Success() {
        // Given
        when(medicalRecordRepository.findById(100L)).thenReturn(Optional.of(mockRecord));
        when(medicineRepository.findById(50L)).thenReturn(Optional.of(mockMedicine));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(mockSavedPrescription);

        // When
        PrescriptionResponse response = prescriptionService.createPrescription(validRequest);

        // Then
        assertNotNull(response, "Dữ liệu đơn thuốc trả về không được phép null");
        assertEquals(888L, response.getId(), "Mã ID đơn thuốc phải trùng khớp với DB");

        verify(prescriptionRepository, times(1)).save(any(Prescription.class));
        verify(medicalRecordRepository, times(1)).findById(100L);
        verify(medicineRepository, times(1)).findById(50L);
    }

    @Test
    @DisplayName("Kịch bản 1.2: Kê đơn thất bại - Hồ sơ bệnh án không tồn tại trên hệ thống")
    void createPrescription_ThrowsException_WhenMedicalRecordNotFound() {
        // Given
        when(medicalRecordRepository.findById(100L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            prescriptionService.createPrescription(validRequest);
        });

        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Kịch bản 1.3: Kê đơn thất bại - Mã thuốc trong đơn không tồn tại trong kho danh mục")
    void createPrescription_ThrowsException_WhenMedicineNotFound() {
        // Given
        when(medicalRecordRepository.findById(100L)).thenReturn(Optional.of(mockRecord));
        when(medicineRepository.findById(50L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            prescriptionService.createPrescription(validRequest);
        });

        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    
    @Test
    @DisplayName("Kịch bản 2.1: Lấy chi tiết đơn thuốc thành công dựa vào ID hợp lệ")
    void getPrescriptionById_Success() {
        // Given
        when(prescriptionRepository.findById(888L)).thenReturn(Optional.of(mockSavedPrescription));

        // When
        PrescriptionResponse response = prescriptionService.getPrescriptionById(888L);

        // Then
        assertNotNull(response, "Dữ liệu phản hồi chi tiết đơn thuốc không được phép null");
        assertEquals(888L, response.getId());
        verify(prescriptionRepository, times(1)).findById(888L);
    }

    @Test
    @DisplayName("Kịch bản 2.2: Lấy chi tiết đơn thuốc thất bại - ID đơn thuốc không tồn tại")
    void getPrescriptionById_ThrowsException_WhenNotFound() {
        // Given: Giả lập không tìm thấy đơn thuốc ID 999
        when(prescriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            prescriptionService.getPrescriptionById(999L);
        });

        verify(prescriptionRepository, times(1)).findById(999L);
    }
}
