package com.hospital.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.entity.Doctor;
import com.hospital.entity.Medicine;
import com.hospital.entity.Patient;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.MedicineRepository;
import com.hospital.repository.PatientRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

/**
 * BỘ KHUNG INTEGRATION TEST CHO LUỒNG LIÊN HOÀN (Task T21)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc // Tự động cấu hình MockMvc để giả lập gọi API HTTP (Đầu việc 1)
@Transactional // Tự động rollback (xóa sạch) dữ liệu thử nghiệm sau khi kết thúc mỗi Test Case
public class AppointmentPrescriptionIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper; // Công cụ chuyển đổi Object <-> JSON

    // Bơm các Repository thực tế để chuẩn bị nạp dữ liệu mồi vào database test
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    // Lưu lại ID của các dữ liệu mồi để các hàm test phía sau sử dụng làm tham số
    protected Long seededPatientId;
    protected Long seededDoctorId;
    protected Long seededMedicineId;

    protected Patient savedPatient;
    protected Doctor savedDoctor;
    protected Medicine savedMedicine;

    @BeforeEach
    void setUpSeedData() {
        Patient patient = new Patient();
        patient.setFullName("Trần Quang Vinh");
        patient.setDob(LocalDate.of(1998, 10, 24));
        patient.setPhone("0911999888");
        patient.setCccd("001200300405");
        patient.setAddress("123 Đường Ba Đình, Hà Nội");
        
        savedPatient = patientRepository.save(patient);
        this.seededPatientId = savedPatient.getId();

        // 2. NẠP DỮ LIỆU MỒI BÁC SĨ
        Doctor doctor = new Doctor();
        doctor.setFullName("PGS.TS Nguyễn Xuân Hùng");
        // Giả sử thực thể Doctor của nhóm có các trường cơ bản như chuyên khoa, số điện thoại...
        // doctor.setSpecialty("Tim mạch");
        
        savedDoctor = doctorRepository.save(doctor);
        this.seededDoctorId = savedDoctor.getId();

        // 3. NẠP DỮ LIỆU MỒI THUỐC VÀO KHO DANH MỤC
        Medicine medicine = new Medicine();
        medicine.setMedicineName("Amoxicillin 500mg");
        // Giả sử thực thể Medicine của nhóm quản lý đơn vị tính
        // medicine.setUnit("Viên");
        
        savedMedicine = medicineRepository.save(medicine);
        this.seededMedicineId = savedMedicine.getId();

        System.out.println("=== ĐÃ NẠP DỮ LIỆU MỒI THÀNH CÔNG VÀO DATABASE INTEGRATION TEST ===");
        System.out.println("Patient ID: " + seededPatientId);
        System.out.println("Doctor ID: " + seededDoctorId);
        System.out.println("Medicine ID: " + seededMedicineId);
        System.out.println("==================================================================");
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Hành trình liên hoàn: Bệnh nhân đặt lịch -> Tiếp nhận phòng khám -> Bác sĩ kê đơn thuốc")
    void vinh_masterIntegrationFlowTest() throws Exception {
        
        // BƯỚC 3.1: GIẢ LẬP ĐẶT LỊCH KHÁM (POST /api/appointments)
        com.hospital.dto.request.AppointmentRequest appointmentRequest = new com.hospital.dto.request.AppointmentRequest();
        appointmentRequest.setPatientId(this.seededPatientId);
        appointmentRequest.setDoctorId(this.seededDoctorId);
        appointmentRequest.setApptDatetime(java.time.LocalDateTime.now().plusDays(1));
        appointmentRequest.setEmail("quangvinh_test@gmail.com");

        String appointmentJson = objectMapper.writeValueAsString(appointmentRequest);

        // Giả lập Frontend gửi lệnh Đặt lịch hẹn
        String appointmentResponseContent = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/appointments")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(appointmentJson))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()) // Hoặc .isCreated() tùy thuộc Controller của ông
                .andReturn().getResponse().getContentAsString();

        // Đọc thông tin ID lịch hẹn vừa được sinh ra tự động từ DB
        com.hospital.dto.response.AppointmentResponse appointmentResponse = 
                objectMapper.readValue(appointmentResponseContent, com.hospital.dto.response.AppointmentResponse.class);
        Long generatedAppointmentId = appointmentResponse.getId();
        
        org.junit.jupiter.api.Assertions.assertNotNull(generatedAppointmentId, "Sau khi đặt lịch, ID cuộc hẹn phải được sinh ra");

        // BƯỚC 3.2: GIẢ LẬP TIẾP NHẬN TẠI QUẦY (Cập nhật trạng thái lịch hẹn)
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/appointments/" + generatedAppointmentId + "/status")
                        .param("status", "CONFIRMED") // Truyền enum thông qua Request Param đúng chuẩn thiết kế của ông
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());


        // BƯỚC 3.3: GIẢ LẬP BÁC SĨ KHÁM VÀ KÊ ĐƠN THUỐC (POST /api/prescriptions)
        com.hospital.dto.request.PrescriptionItemRequest medicineItem = new com.hospital.dto.request.PrescriptionItemRequest();
        medicineItem.setMedicationId(this.seededMedicineId);
        medicineItem.setQuantity(10);
        medicineItem.setDosage("Sáng 1 viên, tối 1 viên sau ăn");

        com.hospital.dto.request.PrescriptionRequest prescriptionRequest = new com.hospital.dto.request.PrescriptionRequest();
        
        prescriptionRequest.setMedicalRecordId(100L); // Thay bằng ID mồi hoặc ID thực tế phát sinh của nhóm ông
        prescriptionRequest.setItems(java.util.Collections.singletonList(medicineItem));

        String prescriptionJson = objectMapper.writeValueAsString(prescriptionRequest);

        // Kích hoạt API kê đơn thuốc thực tế của Task T17
        String prescriptionResponseContent = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/prescriptions")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(prescriptionJson))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        com.hospital.dto.response.PrescriptionResponse finalPrescription = 
                objectMapper.readValue(prescriptionResponseContent, com.hospital.dto.response.PrescriptionResponse.class);
        
        org.junit.jupiter.api.Assertions.assertNotNull(finalPrescription, "Hệ thống phải trả về thông tin đơn thuốc sau khi kê thành công");
        org.junit.jupiter.api.Assertions.assertNotNull(finalPrescription.getId(), "Đơn thuốc thật lưu xuống DB phải được cấp ID chính thức");
        
        System.out.println("-> Đã Đặt lịch thành công với ID cuộc hẹn: " + generatedAppointmentId);
        System.out.println("-> Đã Kê đơn thuốc thành công với ID đơn: " + finalPrescription.getId());
    }
}
