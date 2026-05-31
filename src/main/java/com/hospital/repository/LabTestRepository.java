package com.hospital.repository;

import com.hospital.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for lab tests. */
public interface LabTestRepository extends JpaRepository<LabTest, Long> {
}
