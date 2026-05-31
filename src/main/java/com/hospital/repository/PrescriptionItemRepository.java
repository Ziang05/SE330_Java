package com.hospital.repository;

import com.hospital.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for prescription line items. */
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {
}
