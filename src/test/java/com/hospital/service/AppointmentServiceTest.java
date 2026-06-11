package com.hospital.service;

import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentResponse;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.enums.AppointmentStatus;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Test bọc luồng xử lý Nghiệp vụ Lịch hẹn (Appointment Service).
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    // 1. KHỞI TẠO CẤU TRÚC MOCKITO
    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private EmailService emailService; // Giả lập luồng gửi mail ngầm của Task T19

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private AppointmentRequest validRequest;
    private Patient mockPatient;
    private Doctor mockDoctor;
    private Appointment mockSavedAppointment;

    @BeforeEach
    void setUp() {
        // Chuẩn bị dữ liệu DTO Request từ Frontend gửi lên
        validRequest = new AppointmentRequest();
        validRequest.setPatientId(10L);
        validRequest.setDoctorId(5L);
        validRequest.setApptDatetime(LocalDateTime.of(2026, 6, 15, 9, 0));
        validRequest.setEmail("patient_vinh@gmail.com"); // Đính kèm email nhận thông báo

        // Giả lập thực thể Bệnh nhân (Khớp với file Patient.java ông gửi)
        mockPatient = new Patient();
        mockPatient.setId(10L);
        mockPatient.setFullName("Nguyễn Hoàng Long");
        mockPatient.setPhone("0901234567");

        // Giả lập thực thể Bác sĩ
        mockDoctor = new Doctor();
        mockDoctor.setId(5L);
        mockDoctor.setFullName("Bác sĩ Phạm Gia Bảo");

        // Giả lập thực thể Lịch hẹn sau khi lưu xuống Cơ sở dữ liệu thành công
        mockSavedAppointment = new Appointment();
        mockSavedAppointment.setId(100L); // Có ID sinh tự động từ DB
        mockSavedAppointment.setPatient(mockPatient);
        mockSavedAppointment.setDoctor(mockDoctor);
        mockSavedAppointment.setApptDatetime(validRequest.getApptDatetime());
    }

    // 2. VIẾT CÁC KỊCH BẢN TEST LUỒNG TẠO LỊCH HẸN

    @Test
    @DisplayName("Kịch bản 2.1: Tạo lịch hẹn thành công - Dữ liệu Request hợp lệ và kích hoạt gửi Mail ngầm")
    void createAppointment_Success() {
        // Given (Giả lập hành vi của các tầng phụ thuộc)
        when(patientRepository.findById(10L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findById(5L)).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockSavedAppointment);

        // When (Kích hoạt chạy hàm nghiệp vụ chính)
        AppointmentResponse response = appointmentService.createAppointment(validRequest);

        // Then (Kiểm chứng kết quả đầu ra đúng chuẩn)
        assertNotNull(response, "Dữ liệu trả về không được phép null");
        assertEquals(100L, response.getId(), "Mã ID lịch hẹn phải khớp với bản ghi từ DB");
        assertEquals("Nguyễn Hoàng Long", response.getPatientName());
        assertEquals("Bác sĩ Phạm Gia Bảo", response.getDoctorName());
        assertEquals(validRequest.getApptDatetime(), response.getApptDatetime());
        // Kiểm chứng tính đúng đắn của việc lưu trữ và gửi thông báo ngầm
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        
        // CỰC KỲ QUAN TRỌNG: Kiểm tra xem hàm sendAppointmentConfirmationEmail có được kích hoạt 
        // với đúng đối tượng lịch hẹn và đúng địa chỉ email nhận hay không!
        verify(emailService, times(1)).sendAppointmentConfirmationEmail(eq(mockSavedAppointment), eq("patient_vinh@gmail.com"));
    }

    @Test
    @DisplayName("Kịch bản 2.2: Tạo lịch hẹn thất bại - Không tìm thấy thông tin Bệnh nhân")
    void createAppointment_ThrowsException_WhenPatientNotFound() {
        // Given: Giả lập repository trả về trống (Không tìm thấy bệnh nhân ID 10)
        when(patientRepository.findById(10L)).thenReturn(Optional.empty());

        // When & Then: Ép hệ thống chạy và kiểm chứng xem có ném ra lỗi ResourceNotFoundException 3 tham số hay không
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.createAppointment(validRequest);
        });

        // Kiểm tra xem câu báo lỗi hoặc tham số truyền vào Custom Exception có chính xác không
        assertNotNull(exception);
        
        // Xác nhận hàm lưu DB và gửi mail tuyệt đối KHÔNG ĐƯỢC PHÉP chạy khi đã dính lỗi tìm kiếm
        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(emailService, never()).sendAppointmentConfirmationEmail(any(), anyString());
    }

    @Test
    @DisplayName("Kịch bản 2.3: Tạo lịch hẹn thất bại - Không tìm thấy thông tin Bác sĩ")
    void createAppointment_ThrowsException_WhenDoctorNotFound() {
        // Given: Tìm thấy bệnh nhân nhưng không tìm thấy bác sĩ ID 5
        when(patientRepository.findById(10L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findById(5L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.createAppointment(validRequest);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(emailService, never()).sendAppointmentConfirmationEmail(any(), anyString());
    }

    @Test
    @DisplayName("Kịch bản 3.1: Hủy lịch hẹn thành công - Cập nhật trạng thái cuộc hẹn sang CANCELLED")
    void cancelAppointment_Success() {
        // Given
        Appointment mockActiveAppointment = new Appointment();
        mockActiveAppointment.setId(200L);
        mockActiveAppointment.setPatient(mockPatient);
        mockActiveAppointment.setDoctor(mockDoctor);

        // Giả lập Repository trả về cuộc hẹn gốc
        when(appointmentRepository.findById(200L)).thenReturn(Optional.of(mockActiveAppointment));
        
        // Giả lập sau khi lưu, trạng thái mới đã được cập nhật thành công xuống DB
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockActiveAppointment);

        // When: Gọi chính xác hàm updateAppointmentStatus trong interface của ông kèm enum hủy lịch
        AppointmentResponse response = appointmentService.updateAppointmentStatus(200L, AppointmentStatus.CANCELLED);

        // Then: Kiểm chứng xem hệ thống có thực hiện tìm kiếm và lưu lại trạng thái mới không
        assertNotNull(response, "Dữ liệu phản hồi không được null");
        verify(appointmentRepository, times(1)).findById(200L);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Kịch bản 3.2: Hủy lịch hẹn thất bại - Không tìm thấy mã lịch hẹn cần cập nhật trạng thái")
    void cancelAppointment_ThrowsException_WhenAppointmentNotFound() {
        // Given: Giả lập không tìm thấy cuộc hẹn ID 999
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then: Kiểm tra xem hàm update có bắn ra đúng Custom Exception 3 tham số hay không
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.updateAppointmentStatus(999L, AppointmentStatus.CANCELLED);
        });

        // Xác nhận: Tuyệt đối không lưu (save) dữ liệu rác khi dính lỗi tìm kiếm
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Kịch bản 3.3: Xuất PDF phiếu hẹn thành công - Trả về luồng dữ liệu nhị phân chuẩn khổ A5")
    void exportAppointmentSlipPdf_Success() {
        // Given: Dựng dữ liệu lịch hẹn đầy đủ để sẵn sàng đưa vào phôi vẽ PDF của PdfGeneratorUtil
        Appointment mockPdfAppointment = new Appointment();
        mockPdfAppointment.setId(555L);
        mockPdfAppointment.setPatient(mockPatient);
        mockPdfAppointment.setDoctor(mockDoctor);
        mockPdfAppointment.setApptDatetime(LocalDateTime.of(2026, 6, 15, 14, 30));

        when(appointmentRepository.findById(555L)).thenReturn(Optional.of(mockPdfAppointment));

        // When: Kích hoạt luồng Service kết xuất file nhị phân thô
        java.io.ByteArrayInputStream pdfStream = appointmentService.exportAppointmentSlipPdf(555L);

        // Then: Kiểm chứng luồng file nhị phân xuất ra bộ nhớ (In-memory stream)
        assertNotNull(pdfStream, "Luồng PDF trả về cho controller tuyệt đối không được null");
        assertTrue(pdfStream.available() > 0, "Dung lượng tệp tin PDF sinh ra phải lớn hơn 0 byte để tránh file trắng");
        
        // Đo đạc hành vi cô lập
        verify(appointmentRepository, times(1)).findById(555L);
    }
}
