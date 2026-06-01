package com.hospital.util;

import com.hospital.dto.request.MedicineRequest;
import com.hospital.dto.response.MedicineResponse;
import com.hospital.entity.Medicine;

/** Mapper utility for Medicine entity ↔ DTO conversions. */
public final class MedicineMapper {

    private MedicineMapper() {
    }

    /** Tạo entity mới từ request (dùng khi CREATE). */
    public static Medicine toEntity(MedicineRequest request) {
        Medicine medicine = new Medicine();
        copyToEntity(request, medicine);
        return medicine;
    }

    /** Copy dữ liệu từ request sang entity đã tồn tại (dùng khi UPDATE). */
    public static void copyToEntity(MedicineRequest request, Medicine medicine) {
        medicine.setMedicineName(request.getMedicineName());
        medicine.setGenericName(request.getGenericName());
        medicine.setCategory(request.getCategory());
        medicine.setUnit(request.getUnit());
        medicine.setUnitPrice(request.getUnitPrice());
        medicine.setInsuranceCovered(request.isInsuranceCovered());
    }

    /** Chuyển entity thành response DTO để trả về client. */
    public static MedicineResponse toResponse(Medicine medicine) {
        return new MedicineResponse(
                medicine.getId(),
                medicine.getMedicineName(),
                medicine.getGenericName(),
                medicine.getCategory(),
                medicine.getUnit(),
                medicine.getUnitPrice(),
                medicine.isInsuranceCovered(),
                medicine.isActive(),
                medicine.getCreatedAt(),
                medicine.getUpdatedAt()
        );
    }
}
