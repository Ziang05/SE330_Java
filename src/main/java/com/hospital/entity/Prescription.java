package com.hospital.entity;

import com.hospital.entity.enums.PrescriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Prescription header issued from a medical record.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "prescriptions")
public class Prescription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PrescriptionStatus status = PrescriptionStatus.DRAFT;
}
