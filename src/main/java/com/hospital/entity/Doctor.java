package com.hospital.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Doctor profile and department assignment.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "doctors")
public class Doctor extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "license_number", unique = true, length = 50)
    private String licenseNumber;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
