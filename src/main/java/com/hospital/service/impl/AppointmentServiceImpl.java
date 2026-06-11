package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentResponse;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.enums.AppointmentStatus;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.service.AppointmentService;
import com.hospital.service.EmailService;
import com.hospital.util.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    @Auditable(action = "CREATE", entityType = "Appointment")
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        log.info("Bắt đầu xử lý tạo lịch hẹn cho Patient ID: {} với Doctor ID: {}", 
                request.getPatientId(), request.getDoctorId());

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));

        LocalDateTime startRange = request.getApptDatetime().minusMinutes(29);
        LocalDateTime endRange = request.getApptDatetime().plusMinutes(29);
        
        List<Appointment> conflictingAppointments = appointmentRepository
                .findByDoctorAndApptDatetimeBetween(doctor, startRange, endRange);

        
        long activeConflicts = conflictingAppointments.stream()
                .filter(appt -> appt.getStatus() != AppointmentStatus.CANCELLED)
                .count();

        if (activeConflicts > 0) {
            log.error("Trùng lịch: Doctor ID {} đã có lịch hẹn hoạt động trong khoảng từ {} đến {}", 
                    doctor.getId(), startRange, endRange);
            throw new BusinessException("Bác sĩ đã có lịch hẹn khám khác trong khung giờ này. Vui lòng chọn giờ khác!");
        }

        Appointment appointment = appointmentMapper.toEntity(request, patient, doctor);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        try {
            if (request != null && request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                
            String recipientEmail = request.getEmail().trim();
            log.info("Lịch hẹn ID #{} lưu thành công. Phát hiện email khách hàng từ Request. Tiến hành gửi thư tới: {}", 
                        savedAppointment.getId(), recipientEmail);
                
            emailService.sendAppointmentConfirmationEmail(savedAppointment, recipientEmail);
            } else {
            log.warn("Lịch hẹn ID #{} được tạo nhưng Request không đính kèm thông tin Email nhận thông báo.", savedAppointment.getId());
            }
        } catch (Exception ex) {
                log.error("Hệ thống gặp sự cố ngoài ý muốn khi kích hoạt gửi email thông báo: {}", ex.getMessage());
        }

        log.info("Tạo lịch hẹn thành công! Appointment ID: {}", savedAppointment.getId());
        return appointmentMapper.toResponse(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        log.info("Truy vấn chi tiết lịch hẹn ID: {}", id);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        log.info("Truy vấn toàn bộ danh sách lịch hẹn");
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatientId(Long patientId) {
        log.info("Truy vấn danh sách lịch hẹn của Patient ID: {}", patientId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        return appointmentRepository.findByPatient(patient).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE_STATUS", entityType = "Appointment")
    public AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus newStatus) {
        log.info("Yêu cầu cập nhật trạng thái lịch hẹn ID: {} sang trạng thái: {}", id, newStatus);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        AppointmentStatus currentStatus = appointment.getStatus();

        
        if (currentStatus == AppointmentStatus.CANCELLED || currentStatus == AppointmentStatus.COMPLETED) {
            log.error("Lỗi nghiệp vụ: Lịch hẹn đang ở trạng thái cuối {}, không thể đổi sang {}", currentStatus, newStatus);
            throw new BusinessException("Lịch hẹn đã hủy hoặc đã hoàn thành, không thể thay đổi trạng thái nữa!");
        }

        
        appointment.setStatus(newStatus);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        log.info("Cập nhật trạng thái lịch hẹn ID {} thành công sang {}", id, newStatus);
        return appointmentMapper.toResponse(updatedAppointment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isDoctorConflicted(Long doctorId, LocalDateTime apptDatetime) {
        log.info("Kiểm tra conflict lịch khám nhanh cho Doctor ID: {} tại thời điểm: {}", doctorId, apptDatetime);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        LocalDateTime startRange = apptDatetime.minusMinutes(29);
        LocalDateTime endRange = apptDatetime.plusMinutes(29);

        List<Appointment> conflictingAppointments = appointmentRepository
                .findByDoctorAndApptDatetimeBetween(doctor, startRange, endRange);

        long activeConflicts = conflictingAppointments.stream()
                .filter(appt -> appt.getStatus() != AppointmentStatus.CANCELLED)
                .count();

        return activeConflicts > 0;
    }
}
