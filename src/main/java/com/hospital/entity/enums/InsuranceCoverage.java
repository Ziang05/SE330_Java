package com.hospital.entity.enums;

/**
 * BHYT coverage rate applied when computing insurance deduction on an invoice.
 * FULL  – 100% covered (e.g. emergency treatment)
 * EIGHTY – 80% covered (standard rate)
 * NONE  – not covered (out-of-pocket)
 */
public enum InsuranceCoverage {
    FULL,
    EIGHTY,
    NONE
}
