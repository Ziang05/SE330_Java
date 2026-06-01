package com.hospital.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Request payload for creating or updating a medicine catalog entry. */
@Getter
@Setter
@NoArgsConstructor
public class MedicineRequest {

    @NotBlank(message = "Tên thuốc không được để trống")
    @Size(max = 150, message = "Tên thuốc tối đa 150 ký tự")
    private String medicineName;

    @Size(max = 150, message = "Tên hoạt chất tối đa 150 ký tự")
    private String genericName;

    @Size(max = 80, message = "Nhóm thuốc tối đa 80 ký tự")
    private String category;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 30, message = "Đơn vị tính tối đa 30 ký tự")
    private String unit;

    @NotNull(message = "Giá niêm yết không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá thuốc phải lớn hơn 0")
    private BigDecimal unitPrice;

    /** true = thuốc thuộc danh mục BHYT chi trả. */
    private boolean insuranceCovered = false;
}
