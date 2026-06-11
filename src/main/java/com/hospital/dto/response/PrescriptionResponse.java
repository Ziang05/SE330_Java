package com.hospital.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO tổng hợp chứa thông tin đơn thuốc hoàn chỉnh trả về cho client.
 */
@Getter
@Setter
@NoArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private Long medicalRecordId;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private String doctorNotes;
    private LocalDateTime createdAt;
    
    // Danh sách các loại thuốc chi tiết đi kèm
    private List<PrescriptionItemResponse> items;
}
