package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO hứng dữ liệu kết quả xét nghiệm dạng chữ do Kỹ thuật viên nhập.
 */
@Getter
@Setter
@NoArgsConstructor
public class LabTestResultRequest {

    @NotBlank(message = "Kết quả xét nghiệm không được để trống")
    @Size(max = 2000, message = "Nội dung kết quả không được vượt quá 2000 ký tự")
    private String result;
}
