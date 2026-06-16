package com.hospital.util;

import com.hospital.dto.response.InvoiceResponse;
import com.hospital.entity.Invoice;
import com.hospital.entity.LabTest;
import com.hospital.entity.Prescription;
import com.hospital.entity.PrescriptionItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper utility for Invoice entity ↔ DTO conversions.
 */
public final class InvoiceMapper {

    private InvoiceMapper() {
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(invoice.getId());
        r.setMedicalRecordId(invoice.getMedicalRecord().getId());
        r.setPatientId(invoice.getPatient().getId());
        r.setPatientName(invoice.getPatient().getFullName());
        r.setExaminationFee(invoice.getExaminationFee());
        r.setMedicineFee(invoice.getMedicineFee());
        r.setLabFee(invoice.getLabFee());
        r.setTotalAmount(invoice.getTotalAmount());
        r.setInsuranceCoverage(invoice.getInsuranceCoverage());
        r.setInsuranceAmount(invoice.getInsuranceAmount());
        r.setPaidAmount(invoice.getPaidAmount());
        r.setPaymentMethod(invoice.getPaymentMethod());
        r.setStatus(invoice.getStatus());
        r.setPaidAt(invoice.getPaidAt());
        r.setNotes(invoice.getNotes());
        r.setCreatedAt(invoice.getCreatedAt());
        r.setUpdatedAt(invoice.getUpdatedAt());
        r.setLabTests(new ArrayList<>());
        r.setPrescriptions(new ArrayList<>());
        return r;
    }

    /**
     * Map to response with full detail lists (lab tests + prescriptions).
     */
    public static InvoiceResponse toResponseWithDetails(
            Invoice invoice,
            List<LabTest> labTests,
            List<Prescription> prescriptions
    ) {
        InvoiceResponse r = toResponse(invoice);

        // Lab test line items
        List<InvoiceResponse.LabTestLineItem> labItems = new ArrayList<>();
        if (labTests != null) {
            for (LabTest lt : labTests) {
                labItems.add(new InvoiceResponse.LabTestLineItem(
                        lt.getId(),
                        lt.getTestType(),
                        null,
                        lt.getFee(),
                        lt.getStatus() != null ? lt.getStatus().name() : null
                ));
            }
        }
        r.setLabTests(labItems);

        // Prescription line items
        List<InvoiceResponse.PrescriptionLineItem> rxItems = new ArrayList<>();
        if (prescriptions != null) {
            for (Prescription rx : prescriptions) {
                List<InvoiceResponse.MedicineLineItem> meds = new ArrayList<>();
                if (rx.getPrescriptionItems() != null) {
                    for (PrescriptionItem pi : rx.getPrescriptionItems()) {
                        BigDecimal subtotal = pi.getUnitPriceAtTime()
                                .multiply(BigDecimal.valueOf(pi.getQuantity()));
                        meds.add(new InvoiceResponse.MedicineLineItem(
                                pi.getId(),
                                pi.getMedicine() != null ? pi.getMedicine().getMedicineName() : null,
                                pi.getQuantity(),
                                pi.getMedicine() != null ? pi.getMedicine().getUnit() : null,
                                pi.getDosage(),
                                pi.getUnitPriceAtTime(),
                                subtotal
                        ));
                    }
                }
                rxItems.add(new InvoiceResponse.PrescriptionLineItem(
                        rx.getId(),
                        rx.getDoctor() != null ? rx.getDoctor().getFullName() : null,
                        null,
                        meds
                ));
            }
        }
        r.setPrescriptions(rxItems);

        return r;
    }
}
