package com.hospital.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO phẳng hóa trả về toàn bộ thông tin chi tiết của một phiếu Xét nghiệm.
 */
@Getter
@Setter
@NoArgsConstructor
public class LabTestResponse {
    
    private Long id;                  // ID của phiếu xét nghiệm
    private Long medicalRecordId;     // ID hồ sơ khám bệnh gốc
    private String testName;          // Tên loại xét nghiệm
    private String description;       // Yêu cầu chỉ định từ bác sĩ
    private String result;            // Kết quả (nếu có)
    private String resultFileUrl;     // Đường dẫn URL để xem file ảnh/PDF kết quả đính kèm
    private String status;            // Trạng thái phiếu: PENDING (Chờ), COMPLETED (Đã có kết quả)
    
    // Thông tin tóm tắt phục vụ hiển thị nhanh
    private Long patientId;
    private String patientName;
    private String doctorName;        // Tên bác sĩ ra chỉ định
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
