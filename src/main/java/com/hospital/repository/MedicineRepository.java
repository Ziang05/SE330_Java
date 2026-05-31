package com.hospital.repository;

import com.hospital.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repository for medicine catalog search. */
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByMedicineNameContainingIgnoreCase(String medicineName);

    List<Medicine> findByCategory(String category);
}
