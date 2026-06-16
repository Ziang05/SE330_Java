package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.CheckInRequest;
import com.hospital.dto.response.MedicalRecordResponse;
import com.hospital.entity.Appointment;
import com.hospital.entity.Invoice;
import com.hospital.entity.LabTest;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Prescription;
import com.hospital.entity.PrescriptionItem;
import com.hospital.entity.enums.AppointmentStatus;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.InvoiceRepository;
import com.hospital.repository.LabTestRepository;
import com.hospital.repository.MedicalRecordRepository;
import com.hospital.repository.PrescriptionItemRepository;
import com.hospital.repository.PrescriptionRepository;
import com.hospital.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class xử lý logic nghiệp vụ Tiếp nhận bệnh nhân tại quầy (Check-in).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final LabTestRepository labTestRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    @Auditable(action = "CHECK_IN", entityType = "MedicalRecord")
    public MedicalRecordResponse checkInAndCreateRecord(CheckInRequest request) {
        log.info("Bắt đầu xử lý thủ tục Tiếp nhận (Check-in) cho Appointment ID: {}", request.getAppointmentId());

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", request.getAppointmentId()));

        medicalRecordRepository.findByAppointmentId(request.getAppointmentId())
                .ifPresent(record -> {
                    log.error("Lỗi tiếp nhận: Appointment ID {} đã được tiếp nhận từ trước. MedicalRecord ID hiện tại: {}", 
                            appointment.getId(), record.getId());
                    throw new BusinessException("Lịch hẹn này đã được làm thủ tục tiếp nhận rồi, không thể tiếp nhận lại!");
                });

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            log.error("Lỗi nghiệp vụ: Lịch hẹn ID {} đang ở trạng thái {}, không đủ điều kiện Check-in", 
                    appointment.getId(), appointment.getStatus());
            throw new BusinessException("Chỉ có thể tiếp nhận những lịch hẹn đang ở trạng thái ĐÃ XÁC NHẬN (CONFIRMED)!");
        }

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointmentRepository.save(appointment);
        log.info("Đã cập nhật trạng thái Appointment ID {} sang CHECKED_IN", appointment.getId());

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setAppointment(appointment);
        medicalRecord.setPatient(appointment.getPatient());
        medicalRecord.setDoctor(appointment.getDoctor());
        medicalRecord.setVisitDate(LocalDate.now());
        
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            medicalRecord.setNotes(request.getNotes());
        }

        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);
        log.info("Tạo Hồ sơ khám thành công! MedicalRecord ID: {}", savedRecord.getId());

        return mapToResponse(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<MedicalRecordResponse> getMedicalRecordsByPatientId(Long patientId) {
        log.info("Lay danh sach ho so kham cua Patient ID: {}", patientId);
        java.util.List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId);
        return records.stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponse getById(Long id) {
        log.info("Lay ho so kham theo ID: {}", id);
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", id));

        MedicalRecordResponse response = mapToResponse(record);

        // Populate lab tests
        List<LabTest> labTests = labTestRepository.findByMedicalRecordId(id);
        List<MedicalRecordResponse.LabTestSummary> labSummaries = new ArrayList<>();
        for (LabTest lt : labTests) {
            labSummaries.add(new MedicalRecordResponse.LabTestSummary(
                    lt.getId(),
                    lt.getTestType(),
                    null,
                    lt.getFee(),
                    lt.getStatus() != null ? lt.getStatus().name() : null,
                    lt.getResult()
            ));
        }
        response.setLabTests(labSummaries);

        // Populate prescriptions
        List<Prescription> prescriptions = prescriptionRepository.findByMedicalRecordId(id);
        List<MedicalRecordResponse.PrescriptionSummary> rxSummaries = new ArrayList<>();
        for (Prescription rx : prescriptions) {
            List<MedicalRecordResponse.MedicineItem> items = new ArrayList<>();
            List<PrescriptionItem> rxItems = prescriptionItemRepository.findByPrescriptionId(rx.getId());
            for (PrescriptionItem pi : rxItems) {
                BigDecimal subtotal = pi.getUnitPriceAtTime().multiply(BigDecimal.valueOf(pi.getQuantity()));
                items.add(new MedicalRecordResponse.MedicineItem(
                        pi.getId(),
                        pi.getMedicine() != null ? pi.getMedicine().getMedicineName() : null,
                        pi.getQuantity(),
                        pi.getMedicine() != null ? pi.getMedicine().getUnit() : null,
                        pi.getDosage(),
                        pi.getUnitPriceAtTime(),
                        subtotal
                ));
            }
            rxSummaries.add(new MedicalRecordResponse.PrescriptionSummary(
                    rx.getId(),
                    rx.getDoctor() != null ? rx.getDoctor().getFullName() : null,
                    rx.getIssuedDate(),
                    rx.getStatus() != null ? rx.getStatus().name() : null,
                    items
            ));
        }
        response.setPrescriptions(rxSummaries);

        // Populate invoice (if exists)
        Optional<Invoice> invoiceOpt = invoiceRepository.findByMedicalRecordId(id);
        invoiceOpt.ifPresent(inv -> response.setInvoice(new MedicalRecordResponse.InvoiceSummary(
                inv.getId(),
                inv.getExaminationFee(),
                inv.getMedicineFee(),
                inv.getLabFee(),
                inv.getTotalAmount(),
                inv.getInsuranceAmount(),
                inv.getPaidAmount(),
                inv.getStatus() != null ? inv.getStatus().name() : null
        )));

        return response;
    }

    /**
     * Ham helper phang hoa du lieu tu Entity sang DTO Response.
     */
    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        MedicalRecordResponse response = new MedicalRecordResponse();
        response.setId(record.getId());
        response.setAppointmentId(record.getAppointment().getId());
        response.setVisitDate(record.getVisitDate());
        response.setCreatedAt(record.getCreatedAt());

        response.setStatus("CHECKED_IN"); 

        if (record.getPatient() != null) {
            response.setPatientId(record.getPatient().getId());
            response.setPatientName(record.getPatient().getFullName());
            response.setPatientPhone(record.getPatient().getPhone());
            response.setPatientInsuranceNumber(record.getPatient().getInsuranceNumber());
        }

        if (record.getDoctor() != null) {
            response.setDoctorId(record.getDoctor().getId());
            response.setDoctorName(record.getDoctor().getFullName());
            
            if (record.getDoctor().getDepartment() != null) {
                response.setDepartmentName(record.getDoctor().getDepartment().getDeptName());
            }
        }

        return response;
    }
}
