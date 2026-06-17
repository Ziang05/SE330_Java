package com.hospital.util;

import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentResponse;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    public Appointment toEntity(AppointmentRequest request, Patient patient, Doctor doctor) {
        if (request == null) {
            return null;
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setApptDatetime(request.getApptDatetime());
        appointment.setRepresentativeFullname(request.getRepresentativeFullname());
        appointment.setRepresentativePhone(request.getRepresentativePhone());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(AppointmentStatus.PENDING); // Trạng thái mặc định khi tạo mới

        return appointment;
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setApptDatetime(appointment.getApptDatetime());
        response.setStatus(appointment.getStatus());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        response.setRepresentativeFullname(appointment.getRepresentativeFullname());
        response.setRepresentativePhone(appointment.getRepresentativePhone());

        // Bóc tách thông tin Bệnh nhân an toàn
        if (appointment.getPatient() != null) {
            Patient patient = appointment.getPatient();
            response.setPatientId(patient.getId());
            response.setPatientName(patient.getFullName());
            response.setPatientPhone(patient.getPhone());
        }

        // Bóc tách thông tin Bác sĩ và Phân khoa an toàn
        if (appointment.getDoctor() != null) {
            Doctor doctor = appointment.getDoctor();
            response.setDoctorId(doctor.getId());
            response.setDoctorName(doctor.getFullName());
            
            if (doctor.getDepartment() != null) {
                response.setDepartmentName(doctor.getDepartment().getDeptName());
            }
        }

        return response;
    }
}
