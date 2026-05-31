package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Hospital department such as Internal Medicine or Pediatrics. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @Column(name = "dept_name", nullable = false, length = 120)
    private String deptName;

    @Column(name = "location", length = 120)
    private String location;

    @Column(name = "phone", length = 20)
    private String phone;
}
