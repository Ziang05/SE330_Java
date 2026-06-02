package com.hospital.service;

import com.hospital.dto.request.PaymentRequest;
import com.hospital.dto.response.InvoiceResponse;

import java.util.List;

/**
 * Business contract for payment and invoice management (T39, T40, T44).
 *
 * <p>T39 – Tạo hóa đơn, tính phí, xác nhận thanh toán.
 * <p>T40 – Tự động tạo hóa đơn khi MedicalRecord được hoàn thành.
 * <p>T44 – Xuất hóa đơn thanh toán ra file PDF.
 */
public interface PaymentService {

    /**
     * T40 – Auto-tạo hóa đơn PENDING khi bác sĩ hoàn thành hồ sơ khám.
     * Tính đủ examinationFee + medicineFee + labFee + áp dụng mức BHYT của bệnh nhân.
     *
     * @param medicalRecordId ID hồ sơ khám vừa hoàn thành
     * @return hóa đơn mới ở trạng thái PENDING
     */
    InvoiceResponse createInvoiceForMedicalRecord(Long medicalRecordId);

    /**
     * T39 – Xác nhận thanh toán: chuyển trạng thái PENDING → PAID.
     * Ghi nhận paymentMethod và thời gian thanh toán.
     *
     * @param request invoiceId + paymentMethod
     * @return hóa đơn sau khi thanh toán
     */
    InvoiceResponse processPayment(PaymentRequest request);

    /** Lấy chi tiết một hóa đơn theo ID. */
    InvoiceResponse getById(Long invoiceId);

    /** Lấy toàn bộ hóa đơn của một bệnh nhân (mới nhất trước). */
    List<InvoiceResponse> getByPatientId(Long patientId);

    /**
     * T44 – Xuất hóa đơn thanh toán ra file PDF.
     *
     * <p>Layout giống tờ biên lai: tên bệnh viện, thông tin bệnh nhân,
     * từng khoản phí (khám, thuốc, xét nghiệm), BHYT, tổng phải trả.
     *
     * @param invoiceId ID hóa đơn cần xuất
     * @return mảng byte của file .pdf
     */
    byte[] exportInvoiceToPdf(Long invoiceId);
}
