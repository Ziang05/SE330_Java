package com.hospital.repository;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Repository for appointment schedules. */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorAndApptDatetimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByPatient(Patient patient);
}
