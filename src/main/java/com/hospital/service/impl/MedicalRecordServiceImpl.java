package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.CheckInRequest;
import com.hospital.dto.response.MedicalRecordResponse;
import com.hospital.entity.Appointment;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.enums.AppointmentStatus;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.MedicalRecordRepository;
import com.hospital.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Class xử lý logic nghiệp vụ Tiếp nhận bệnh nhân tại quầy (Check-in).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

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
