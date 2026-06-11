package com.hospital.service;

import com.hospital.entity.Appointment;

public interface EmailService {
    void sendAppointmentConfirmationEmail(Appointment appointment, String recipientEmail);
}
