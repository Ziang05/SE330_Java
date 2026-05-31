package com.hospital.repository;

import com.hospital.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for invoices. */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
