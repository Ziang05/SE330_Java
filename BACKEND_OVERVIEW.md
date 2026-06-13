# BACKEND_OVERVIEW.md

Tài liệu này mô tả trạng thái backend hiện tại dựa trên code trong repository.
Mục tiêu là để frontend dev nhìn vào là biết ngay:
- backend đang có gì thật
- gọi API nào, gửi gì, nhận gì
- endpoint nào cần token và role nào
- những phần nào còn thiếu hoặc chưa hoàn chỉnh

Lưu ý: một số route không đồng nhất prefix giữa các module vẫn được giữ nguyên theo code thực tế.

## 1. Tổng Quan Kiến Trúc

### Tech stack

| Thành phần | Giá trị |
|---|---|
| Framework | Spring Boot 3.2.12 |
| Language | Java 17 |
| Database | MySQL 8.x |
| Build tool | Maven |
| Persistence | Spring Data JPA |
| Validation | Spring Boot Validation |
| Security | Spring Security + JWT |
| API docs | springdoc-openapi 2.5.0 |
| PDF export | OpenPDF |
| Excel export | Apache POI |
| Mail | Spring Mail + Thymeleaf template |
| Utility libs | ZXing, Lombok |

### Cấu trúc package chính

- `entity`: JPA entity và enum
- `repository`: Spring Data repository
- `service` và `service/impl`: interface và implement nghiệp vụ
- `controller`: REST API
- `dto/request`: payload đầu vào
- `dto/response`: payload đầu ra
- `exception`: custom exception và `GlobalExceptionHandler`
- `config`: Swagger/OpenAPI, Security, Web MVC, JWT properties
- `security`: JWT filter, token provider, user details
- `audit`: annotation và AOP cho audit log
- `util`: mapper và helper cho PDF

### Database schema

Schema hiện có trong `src/main/resources/db/schema.sql` và seed data trong `src/main/resources/db/seed_data.sql`.
Các bảng chính:
- `patients`
- `doctors`
- `departments`
- `appointments`
- `medical_records`
- `prescriptions`
- `prescription_items`
- `lab_tests`
- `invoices`
- `medicines`
- `users`
- `roles`
- `user_roles`
- `audit_logs`

### Config và chạy ứng dụng

- Dev profile: `src/main/resources/application-dev.yml`
  - `server.port=8080`
  - MySQL local: `jdbc:mysql://localhost:3306/hospital_db`
  - `ddl-auto=validate`
- Prod profile: `src/main/resources/application-prod.yml`
  - dùng biến môi trường `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SERVER_PORT`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Docker: có `Dockerfile` và `docker-compose.yml`

### Base URL thực tế

- Phần lớn module dùng `/api/v1/...`
- Một số module dùng prefix riêng:
  - `Auth`: `/api/auth`
  - `User`: `/api/users`
  - `Audit Log`: `/api/audit-logs`
  - `Appointment`: `/api/appointments`

## 2. Authentication & Authorization

### Cơ chế security hiện tại

- `SecurityConfig` bật stateless JWT
- CSRF tắt
- CORS đang mở rộng `*` cho origin/method/header
- JWT filter đọc header `Authorization: Bearer <token>`
- `springdoc` swagger endpoints được permit
- tất cả endpoint còn lại yêu cầu authenticated request

### Endpoint auth

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/auth/login` | Public | - | Đăng nhập bằng `username/password` |
| POST | `/api/auth/refresh` | Public | - | Đổi refresh token lấy access token mới |
| POST | `/api/auth/logout` | Token | Any authenticated user | Xóa `SecurityContext` phía server, không blacklist token |
| GET | `/api/auth/me` | Token | Any authenticated user | Trả thông tin user hiện tại |

### Login response

`LoginResponse` trả về:
- `accessToken`
- `refreshToken`
- `tokenType` = `Bearer`
- `expiresIn`
- `userId`
- `username`
- `roles`

### Claims trong access token

- `subject`: `username`
- claims:
  - `userId`
  - `username`
  - `roles`

### Roles hiện có

| Role | Trạng thái hiện tại |
|---|---|
| `ADMIN` | Toàn quyền: master data, user, audit log, report, payment, medicine, doctor, department |
| `DOCTOR` | Xem bệnh nhân, lịch khám, kê đơn, tạo chỉ định xét nghiệm, xem hóa đơn/thuốc |
| `NURSE` | Quản lý bệnh nhân, lịch khám, check-in, cập nhật kết quả xét nghiệm, thanh toán |
| `CASHIER` | Có trong enum và DB, nhưng hiện chưa thấy controller nào gán quyền riêng cho role này |

### Cách gọi API sau khi login

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

### Endpoint public

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /swagger-ui.html`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`

## 3. Danh Sách API Theo Module

### 3.1 Module Patient

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/patients` | Token | ADMIN, NURSE | Tạo bệnh nhân mới |
| GET | `/api/v1/patients/{id}` | Token | ADMIN, DOCTOR, NURSE | Xem chi tiết bệnh nhân |
| GET | `/api/v1/patients` | Token | ADMIN, DOCTOR, NURSE | Lấy danh sách bệnh nhân |
| GET | `/api/v1/patients/search?keyword=...` | Token | ADMIN, DOCTOR, NURSE | Tìm bệnh nhân theo tên |
| PUT | `/api/v1/patients/{id}` | Token | ADMIN, NURSE | Cập nhật bệnh nhân |
| DELETE | `/api/v1/patients/{id}` | Token | ADMIN, NURSE | Xóa bệnh nhân |

### 3.2 Module Doctor

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/doctors` | Token | ADMIN | Tạo bác sĩ |
| GET | `/api/v1/doctors/{doctorId}` | Token | ADMIN | Xem chi tiết bác sĩ |
| GET | `/api/v1/doctors` | Token | ADMIN | Lấy danh sách bác sĩ |
| PUT | `/api/v1/doctors/{doctorId}` | Token | ADMIN | Cập nhật bác sĩ |
| DELETE | `/api/v1/doctors/{doctorId}` | Token | ADMIN | Xóa bác sĩ |

### 3.3 Module Department

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/departments` | Token | ADMIN | Tạo khoa/phòng |
| GET | `/api/v1/departments/{departmentId}` | Token | ADMIN | Xem chi tiết khoa/phòng |
| GET | `/api/v1/departments` | Token | ADMIN | Lấy danh sách khoa/phòng |
| PUT | `/api/v1/departments/{departmentId}` | Token | ADMIN | Cập nhật khoa/phòng |
| DELETE | `/api/v1/departments/{departmentId}` | Token | ADMIN | Xóa khoa/phòng |

### 3.4 Module Appointment

Lưu ý: module này đang dùng base path `/api/appointments` chứ không phải `/api/v1/appointments`.

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/appointments` | Token | ADMIN, NURSE | Đặt lịch khám |
| GET | `/api/appointments/{id}` | Token | ADMIN, DOCTOR, NURSE | Xem chi tiết lịch khám |
| GET | `/api/appointments` | Token | ADMIN, NURSE | Lấy danh sách lịch khám |
| GET | `/api/appointments/patient/{patientId}` | Token | ADMIN, DOCTOR, NURSE | Lấy lịch khám theo bệnh nhân |
| PUT | `/api/appointments/{id}/status?status=...` | Token | ADMIN, NURSE | Cập nhật trạng thái lịch khám |
| GET | `/api/appointments/check-conflict?doctorId=...&apptDatetime=...` | Token | ADMIN, NURSE | Kiểm tra trùng lịch bác sĩ |
| GET | `/api/appointments/{id}/export-pdf` | Token | ADMIN, DOCTOR, NURSE | Xuất phiếu hẹn PDF |

### 3.5 Module Reception

Backend hiện không có entity/repository `Reception` riêng.
Luồng tiếp nhận đang được triển khai qua `MedicalRecord` và endpoint check-in.

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/medical-records/check-in` | Token | ADMIN, NURSE | Tiếp nhận bệnh nhân, tạo hồ sơ khám |

### 3.6 Module Prescription

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/prescriptions` | Token | ADMIN, DOCTOR | Kê đơn thuốc mới |
| GET | `/api/v1/prescriptions/{id}` | Token | ADMIN, DOCTOR, NURSE | Xem chi tiết đơn thuốc |
| GET | `/api/v1/prescriptions/{id}/export-pdf` | Token | ADMIN, DOCTOR, NURSE | Xuất đơn thuốc PDF |

### 3.7 Module Lab Test

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/lab-tests` | Token | ADMIN, DOCTOR | Tạo phiếu chỉ định xét nghiệm |
| PUT | `/api/v1/lab-tests/{id}/result` | Token | ADMIN, NURSE | Cập nhật kết quả xét nghiệm, upload file |
| GET | `/api/v1/lab-tests/pending` | Token | ADMIN, DOCTOR, NURSE | Lấy danh sách phiếu đang chờ xử lý |

### 3.8 Module Payment

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/invoices/medical-records/{medicalRecordId}` | Token | ADMIN, NURSE, DOCTOR | Auto tạo hóa đơn sau khám |
| POST | `/api/v1/invoices/pay` | Token | ADMIN, NURSE | Thanh toán hóa đơn |
| GET | `/api/v1/invoices/{id}` | Token | ADMIN, NURSE, DOCTOR | Xem chi tiết hóa đơn |
| GET | `/api/v1/invoices/patient/{patientId}` | Token | ADMIN, NURSE, DOCTOR | Lấy lịch sử hóa đơn của bệnh nhân |
| GET | `/api/v1/invoices/{id}/export` | Token | ADMIN, NURSE | Xuất hóa đơn PDF |

### 3.9 Module Medicine

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| GET | `/api/v1/medicines` | Token | ADMIN, DOCTOR, NURSE | Lấy danh sách thuốc |
| GET | `/api/v1/medicines/{id}` | Token | ADMIN, DOCTOR, NURSE | Xem chi tiết thuốc |
| GET | `/api/v1/medicines/search?keyword=...` | Token | ADMIN, DOCTOR, NURSE | Tìm thuốc theo tên |
| GET | `/api/v1/medicines/category?category=...` | Token | ADMIN, DOCTOR, NURSE | Lọc thuốc theo nhóm |
| POST | `/api/v1/medicines` | Token | ADMIN | Tạo thuốc mới |
| PUT | `/api/v1/medicines/{id}` | Token | ADMIN | Cập nhật thuốc |
| DELETE | `/api/v1/medicines/{id}` | Token | ADMIN | Xóa thuốc mềm |

### 3.10 Module Reports

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| GET | `/api/v1/reports/revenue?from=YYYY-MM-DD&to=YYYY-MM-DD` | Token | ADMIN | Báo cáo doanh thu theo khoảng ngày |
| GET | `/api/v1/reports/revenue/today` | Token | ADMIN | Doanh thu hôm nay |
| GET | `/api/v1/reports/revenue/this-month` | Token | ADMIN | Doanh thu tháng hiện tại |
| GET | `/api/v1/reports/revenue/export?from=...&to=...` | Token | ADMIN | Xuất báo cáo doanh thu Excel |

### 3.11 Module User Management

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| GET | `/api/users?keyword=...&isActive=...&page=...&size=...` | Token | ADMIN | Lấy danh sách user có phân trang |
| GET | `/api/users/{id}` | Token | ADMIN | Xem chi tiết user |
| POST | `/api/users` | Token | ADMIN | Tạo user |
| PUT | `/api/users/{id}` | Token | ADMIN | Cập nhật user |
| PATCH | `/api/users/{id}/toggle-active` | Token | ADMIN | Bật/tắt user |
| PATCH | `/api/users/{id}/reset-password` | Token | ADMIN | Reset mật khẩu user |

### 3.12 Module Audit Log

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| GET | `/api/audit-logs?userId=...&action=...&entityType=...&fromDate=...&toDate=...&page=...&size=...` | Token | ADMIN | Lấy audit log có lọc |
| GET | `/api/audit-logs/{id}` | Token | ADMIN | Xem audit log chi tiết |
| GET | `/api/audit-logs/export` | Token | ADMIN | Xuất audit log CSV |

### 3.13 Export endpoints

| Method | Endpoint | File | Auth | Mô tả |
|---|---|---|---|---|
| GET | `/api/appointments/{id}/export-pdf` | PDF inline | ADMIN, DOCTOR, NURSE | Phiếu hẹn |
| GET | `/api/v1/prescriptions/{id}/export-pdf` | PDF inline | ADMIN, DOCTOR, NURSE | Đơn thuốc |
| GET | `/api/v1/invoices/{id}/export` | PDF attachment | ADMIN, NURSE | Hóa đơn |
| GET | `/api/v1/reports/revenue/export` | XLSX attachment | ADMIN | Báo cáo doanh thu |
| GET | `/api/audit-logs/export` | CSV download | ADMIN | Nhật ký hoạt động |

## 4. Data Models

### 4.1 Rule chung

- Hầu hết entity kế thừa `BaseEntity` nên có `id`, `createdAt`, `updatedAt`
- `AuditLog` có `id` riêng và `createdAt` riêng
- DTO request dùng validation annotation như `@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@PastOrPresent`, `@FutureOrPresent`

### 4.2 Authentication models

| Model | Fields | Notes |
|---|---|---|
| `LoginRequest` | `username*`, `password*` | request login |
| `RefreshTokenRequest` | `refreshToken*` | request refresh |
| `LoginResponse` | `accessToken`, `refreshToken`, `tokenType`, `expiresIn`, `userId`, `username`, `roles` | response sau login/refresh |

### 4.3 Patient

| Model | Fields | Notes |
|---|---|---|
| `PatientRequest` | `fullName*`, `dob`, `gender`, `cccd`, `phone`, `address`, `bloodType`, `insuranceNumber` | create/update |
| `PatientResponse` | `id`, `fullName`, `dob`, `gender`, `cccd`, `phone`, `address`, `bloodType`, `insuranceNumber`, `createdAt`, `updatedAt` | trả về cho UI |

### 4.4 Doctor

| Model | Fields | Notes |
|---|---|---|
| `DoctorRequest` | `fullName*`, `phone`, `email`, `licenseNumber`, `hireDate`, `departmentId` | create/update |
| `DoctorResponse` | `id`, `fullName`, `phone`, `email`, `licenseNumber`, `hireDate`, `departmentId`, `createdAt`, `updatedAt` | trả về cho UI |

### 4.5 Department

| Model | Fields | Notes |
|---|---|---|
| `DepartmentRequest` | `deptName*`, `location`, `phone` | create/update |
| `DepartmentResponse` | `id`, `deptName`, `location`, `phone`, `createdAt`, `updatedAt` | trả về cho UI |

### 4.6 Appointment / Reception

| Model | Fields | Notes |
|---|---|---|
| `AppointmentRequest` | `patientId*`, `doctorId*`, `apptDatetime*`, `email`, `notes` | `apptDatetime` theo ISO date-time |
| `AppointmentResponse` | `id`, `apptDatetime`, `status`, `notes`, `createdAt`, `updatedAt`, `patientId`, `patientName`, `patientPhone`, `doctorId`, `doctorName`, `departmentName` | dùng cho lịch khám |
| `CheckInRequest` | `appointmentId*`, `notes` | dùng cho tiếp nhận/check-in |
| `MedicalRecordResponse` | `id`, `appointmentId`, `visitDate`, `status`, `patientId`, `patientName`, `patientPhone`, `doctorId`, `doctorName`, `departmentName`, `createdAt` | hồ sơ khám sinh ra sau check-in |

### 4.7 Prescription

| Model | Fields | Notes |
|---|---|---|
| `PrescriptionRequest` | `medicalRecordId*`, `doctorNotes`, `items*[]` | đơn thuốc phải có ít nhất 1 item |
| `PrescriptionItemRequest` | `medicationId*`, `quantity*`, `dosage*` | item thuốc |
| `PrescriptionResponse` | `id`, `medicalRecordId`, `doctorId`, `doctorName`, `patientId`, `patientName`, `doctorNotes`, `items`, `createdAt` | đơn thuốc trả về |
| `PrescriptionItemResponse` | `id`, `medicationId`, `medicationName`, `unit`, `quantity`, `dosage` | line item |

### 4.8 Lab Test

| Model | Fields | Notes |
|---|---|---|
| `LabTestCreateRequest` | `medicalRecordId*`, `testName*`, `description` | tạo phiếu xét nghiệm |
| `LabTestResultRequest` | `result*` | text kết quả |
| `LabTestResponse` | `id`, `medicalRecordId`, `testName`, `description`, `result`, `resultFileUrl`, `status`, `patientId`, `patientName`, `doctorName`, `createdAt`, `updatedAt` | kết quả + file |

### 4.9 Payment / Invoice

| Model | Fields | Notes |
|---|---|---|
| `PaymentRequest` | `invoiceId*`, `paymentMethod*` | thanh toán hóa đơn |
| `InvoiceResponse` | `id`, `medicalRecordId`, `patientId`, `patientName`, `examinationFee`, `medicineFee`, `labFee`, `totalAmount`, `insuranceCoverage`, `insuranceAmount`, `paidAmount`, `paymentMethod`, `status`, `paidAt`, `notes`, `createdAt`, `updatedAt` | dùng cho UI và PDF |

### 4.10 Medicine

| Model | Fields | Notes |
|---|---|---|
| `MedicineRequest` | `medicineName*`, `genericName`, `category`, `unit*`, `unitPrice*`, `insuranceCovered` | danh mục thuốc |
| `MedicineResponse` | `id`, `medicineName`, `genericName`, `category`, `unit`, `unitPrice`, `insuranceCovered`, `active`, `createdAt`, `updatedAt` | có `active` để soft delete |

### 4.11 User management

| Model | Fields | Notes |
|---|---|---|
| `CreateUserRequest` | `username*`, `password*`, `email*`, `fullName*`, `roleNames*[]`, `doctorId` | tạo user |
| `UpdateUserRequest` | `email`, `fullName`, `roleNames[]`, `doctorId` | cập nhật user |
| `ResetPasswordRequest` | `newPassword*` | reset mật khẩu |
| `UserResponse` | `id`, `username`, `email`, `fullName`, `isActive`, `roles`, `doctorId`, `createdAt`, `updatedAt` | trả về cho admin |

### 4.12 Audit log

| Model | Fields | Notes |
|---|---|---|
| `AuditLogFilterRequest` | `userId`, `action`, `entityType`, `fromDate`, `toDate`, `page`, `size` | lọc log + phân trang |
| `AuditLogResponse` | `id`, `userId`, `action`, `entityType`, `entityId`, `oldValue`, `newValue`, `ipAddress`, `createdAt` | bản ghi log |

### 4.13 Report

| Model | Fields | Notes |
|---|---|---|
| `RevenueReportResponse` | `fromDate`, `toDate`, `totalRevenue`, `totalInsuranceAmount`, `totalVisits`, `dailyBreakdown[]` | báo cáo doanh thu |
| `DailySummary` | `date`, `revenue`, `visits` | dùng trong biểu đồ / bảng |

### 4.14 Generic envelope

`ApiResponse<T>` có các field:
- `success`
- `message`
- `data`
- `timestamp`

## 5. Các Tính Năng Đặc Biệt

- **Conflict check lịch khám**: khi đặt lịch, backend kiểm tra trùng trong khung giờ `apptDatetime - 29 phút` đến `apptDatetime + 29 phút`; lịch đã `CANCELLED` không tính là conflict.
- **Check-in / tiếp nhận**: chỉ lịch ở trạng thái `CONFIRMED` mới được check-in; sau đó lịch chuyển sang `CHECKED_IN` và tạo `MedicalRecord`.
- **Tính BHYT trên hóa đơn**:
  - có `insuranceNumber` -> áp dụng `EIGHTY` = 80%
  - không có `insuranceNumber` -> `NONE` = 0%
  - enum có `FULL`, nhưng flow tạo hóa đơn hiện tại chỉ gán `EIGHTY` hoặc `NONE`
- **Auto tạo hóa đơn**: từ `MedicalRecord`, cộng phí khám cố định `150000`, cộng phí thuốc từ `PrescriptionItem` và phí xét nghiệm từ `LabTest`.
- **Thanh toán hóa đơn**: `PENDING -> PAID`, lưu `paymentMethod` và `paidAt`.
- **Gửi email xác nhận lịch hẹn**: nếu request có `email`, hệ thống gửi mail xác nhận sau khi tạo appointment.
- **Xuất PDF / Excel / CSV**:
  - phiếu hẹn PDF
  - đơn thuốc PDF
  - hóa đơn PDF
  - báo cáo doanh thu Excel
  - audit log CSV
- **Upload file kết quả xét nghiệm**: endpoint multipart lưu file vào `uploads/` và trả `resultFileUrl`.
- **Audit logging**: các luồng tạo/cập nhật/xóa quan trọng gắn `@Auditable`; AOP sẽ ghi `AuditLog`.

### Hướng dẫn gọi API từ frontend

- JSON request: `Content-Type: application/json`
- `AppointmentRequest.apptDatetime` và `AuditLogFilterRequest.fromDate/toDate` dùng format ISO
- `LabTestController` update result dùng multipart:
  - part `data`: JSON của `LabTestResultRequest`
  - part `file`: file đính kèm, có thể bỏ trống
- Các endpoint export trả về binary:
  - PDF: `application/pdf`
  - Excel: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
  - CSV: `text/csv` qua `HttpServletResponse`

## 6. Error Handling

### Envelope lỗi chuẩn

Hệ thống luôn trả lỗi qua `ApiResponse`:
- `success = false`
- `message` mô tả lỗi
- `data = null` hoặc map validation
- `timestamp` hiện tại

### Các HTTP status chính

| Status | Khi nào xảy ra | Ghi chú |
|---|---|---|
| 400 | Validation fail, `BusinessException` | Ví dụ: lịch trùng, hóa đơn đã paid, xét nghiệm đã completed |
| 401 | Chưa đăng nhập, token sai/hết hạn, login sai | `AuthenticationEntryPoint`, JWT filter, auth handler |
| 403 | Không có quyền role | `AccessDeniedHandler` |
| 404 | Không tìm thấy resource | `ResourceNotFoundException` |
| 409 | Trùng dữ liệu unique | `DuplicateResourceException` |
| 500 | Lỗi runtime không dự đoán trước | fallback trong `GlobalExceptionHandler` |

### Validation response

Khi request body sai validation, response có dạng:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "fullName": "Full name is required"
  },
  "timestamp": "2026-06-12T..."
}
```

## 7. Những Gì Chưa Có / Known Gaps

### Task còn thiếu theo audit

| Task ID | Trạng thái | Thiếu gì | Ảnh hưởng |
|---|---|---|---|
| `T12` | Chưa | Unit test cho `PatientService`, `DoctorService` | Chưa có test coverage cho 2 service lõi |
| `T22` | Chưa | Swagger/OpenAPI doc đầy đủ cho module lịch và đơn thuốc | Frontend chưa có mô tả API chuẩn cho các module này |
| `T24` | Chưa | Test edge case đặt lịch vượt slot và xét nghiệm thiếu kết quả | Có nguy cơ thiếu bảo vệ regression ở case biên |
| `T56` | Chưa | Security test và deployment guide riêng | Chưa có checklist kiểm tra bảo mật và hướng dẫn triển khai riêng |
| `T55` | Thiếu | Không thấy `.github/workflows/*.yml` trong repo | Chưa có workflow CI/CD thực sự |

### Ghi chú thực tế từ code audit

- `README.md` vẫn còn câu mô tả cũ rằng Spring Security/JWT chưa implement, nhưng code hiện tại đã có `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenProvider` thật.
- Không thấy file workflow GitHub Actions trong `.github/workflows`, dù task plan có nhắc tới CI/CD.
- `Reception` không phải là một module/entity riêng; backend đang dùng `MedicalRecord` để xử lý check-in.
- `AppointmentController` đang dùng `/api/appointments` trong khi đa số module khác dùng `/api/v1/...`.
- `CASHIER` đã có trong enum role nhưng chưa thấy map quyền riêng ở controller hiện tại.
- Swagger/OpenAPI đã có `OpenApiConfig`, nhưng các controller lịch/đơn thuốc/xét nghiệm chưa có annotation mô tả chi tiết như `@Operation`/`@ApiResponse` đồng đều.

## 8. Ghi Chú Cho Frontend

- Dùng `accessToken` cho mọi request sau login.
- Khi cần refresh, gửi `refreshToken` lên `/api/auth/refresh`.
- Khi render lịch khám:
  - trạng thái chính: `PENDING`, `CONFIRMED`, `CHECKED_IN`, `COMPLETED`, `CANCELLED`
- Khi render hóa đơn:
  - trạng thái chính: `PENDING`, `PAID`, `CANCELLED`
- Khi render xét nghiệm:
  - trạng thái chính: `ORDERED`, `SAMPLE_COLLECTED`, `COMPLETED`
- Khi render đơn thuốc:
  - trạng thái chính: `DRAFT`, `ISSUED`

## 9. Kết Luận Nhanh

Backend hiện đã có:
- auth JWT + role-based access
- CRUD cho bệnh nhân, bác sĩ, khoa phòng, thuốc, user
- lịch khám + conflict check
- check-in / medical record
- kê đơn + xét nghiệm
- thanh toán + hóa đơn + BHYT
- báo cáo doanh thu
- audit log
- export PDF / Excel / CSV

Điểm cần lưu ý nhất khi tích hợp frontend là:
- route prefix không hoàn toàn đồng nhất
- Swagger cho một vài module vẫn chưa đầy đủ
- có 4 task test/docs/deployment vẫn còn thiếu thật
