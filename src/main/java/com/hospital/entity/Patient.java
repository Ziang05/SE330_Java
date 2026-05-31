package com.hospital.entity;

import com.hospital.entity.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Stores patient identity, contact, and insurance profile. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "patients")
public class Patient extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "dob")
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "cccd", unique = true, length = 20)
    private String cccd;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "blood_type", length = 10)
    private String bloodType;

    @Column(name = "insurance_number", length = 50)
    private String insuranceNumber;
}
