package com.hospital.exception;

/**
 * Raised for domain rule violations.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
