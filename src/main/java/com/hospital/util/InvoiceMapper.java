package com.hospital.util;

import com.hospital.dto.response.InvoiceResponse;
import com.hospital.entity.Invoice;

/** Mapper utility for Invoice entity ↔ DTO conversions. */
public final class InvoiceMapper {

    private InvoiceMapper() {
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getMedicalRecord().getId(),
                invoice.getPatient().getId(),
                invoice.getPatient().getFullName(),
                invoice.getExaminationFee(),
                invoice.getMedicineFee(),
                invoice.getLabFee(),
                invoice.getTotalAmount(),
                invoice.getInsuranceCoverage(),
                invoice.getInsuranceAmount(),
                invoice.getPaidAmount(),
                invoice.getPaymentMethod(),
                invoice.getStatus(),
                invoice.getPaidAt(),
                invoice.getNotes(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}
