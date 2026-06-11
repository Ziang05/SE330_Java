package com.hospital.service.impl;

import com.hospital.entity.Appointment;
import com.hospital.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Thư viện điều khiển trộn HTML của Thymeleaf

    /**
     * Hàm gửi email xác nhận đặt lịch khám thành công.
     * Sử dụng cấu hình @Async để kích hoạt chạy ngầm (Asynchronous thread),
     * giúp luồng API đặt lịch phản hồi tức thì về Frontend mà không bị delay do mạng SMTP của Google.
     */
    @Async
    @Override
    public void sendAppointmentConfirmationEmail(Appointment appointment, String recipientEmail) {
        log.info("Bắt đầu khởi chạy thread gửi email thông báo đặt lịch tới hòm thư: {}", recipientEmail);

        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            log.warn("Bệnh nhân không đăng ký email trên hệ thống, bỏ qua bước gửi mail.");
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            Context context = new Context();
            context.setVariable("patientName", appointment.getPatient() != null ? appointment.getPatient().getFullName() : "Quý khách");
            context.setVariable("appointmentId", "#LH-" + appointment.getId());
            context.setVariable("doctorName", appointment.getDoctor() != null ? appointment.getDoctor().getFullName() : "Bác sĩ trực ban");
            
            String formattedTime = appointment.getApptDatetime() != null ? 
                    appointment.getApptDatetime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Chưa xác định";
            context.setVariable("appointmentTime", formattedTime);
            
            if (appointment.getDoctor() != null && appointment.getDoctor().getDepartment() != null) {
                context.setVariable("departmentName", appointment.getDoctor().getDepartment().getDeptName());
            }

            String htmlContent = templateEngine.process("mail/appointment-confirmation", context);

            helper.setTo(recipientEmail);
            helper.setSubject("🔥 [MediPlus] Xác nhận đặt lịch hẹn khám bệnh thành công #" + appointment.getId());
            helper.setText(htmlContent, true); // Tham số true cực quan trọng xác nhận đây là Email HTML

            mailSender.send(mimeMessage);
            log.info("Đã gửi email thông báo lịch hẹn thành công tới hòm thư: {}", recipientEmail);

        } catch (MessagingException e) {
            log.error("Gặp sự cố nghiêm trọng khi đóng gói cấu trúc email HTML: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi kết nối hoặc xác thực tài khoản với hệ thống Google Mail Server: {}", e.getMessage());
        }
    }
}
