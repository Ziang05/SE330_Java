package com.hospital.service;

import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentResponse;
import com.hospital.entity.enums.AppointmentStatus;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse createAppointment(AppointmentRequest request);

    AppointmentResponse getAppointmentById(Long id);

    List<AppointmentResponse> getAllAppointments();

    List<AppointmentResponse> getAppointmentsByPatientId(Long patientId);

    AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus status);

    boolean isDoctorConflicted(Long doctorId, java.time.LocalDateTime apptDatetime);
}
