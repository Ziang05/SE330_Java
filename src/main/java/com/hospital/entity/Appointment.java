package com.hospital.entity;

import com.hospital.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Scheduled visit between a patient and doctor.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "appt_datetime", nullable = false)
    private LocalDateTime apptDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "representative_fullname", nullable = true, length = 150)
    private String representativeFullname;
    
    @Column(name = "representative_phone", nullable = true, length = 20)
    private String representativePhone;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
