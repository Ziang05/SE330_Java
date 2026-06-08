package com.hospital.repository;

import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for doctors.
 */
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByDepartment(Department department);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);
}
