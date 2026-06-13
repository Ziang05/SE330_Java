# 📋 Phân Tích Backend So Với Yêu Cầu Đề Bài

> Báo cáo đánh giá mức độ đáp ứng yêu cầu của backend dự án **Hospital Management System** so với file [Backend_requirement.md](file:///c:/Users/ASUS/Downloads/SE330_Java/Backend_requirement.md).

---

## Tổng quan nhanh

| Hạng mục | Đã đạt | Thiếu/Cần cải thiện |
|---|---|---|
| **10 Chức năng chính (F01–F10)** | 9/10 đạt cơ bản | F04 thiếu walk-in, F01 thiếu phân trang |
| **Database Schema** | ✅ Khớp đặc tả | — |
| **Bảo mật (JWT, RBAC, BCrypt)** | ✅ Đạt | Thiếu rate limiting |
| **Kiến trúc phân tầng** | ✅ Đạt | — |
| **Docker & Triển khai** | ✅ Đạt (thiếu nginx) | Thiếu CI/CD |
| **API Docs (Swagger)** | ✅ Có | — |
| **Unit Test** | ⚠️ Rất ít (1 file) | Yêu cầu ≥ 70% coverage |
| **Xuất Excel/PDF** | ✅ Đạt | — |

---

## 1. Chức năng chính (F01 – F10)

### ✅ F01. Quản lý bệnh nhân — ĐẠT CƠ BẢN

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| Thêm bệnh nhân | ✅ | `POST /api/v1/patients` — ADMIN, NURSE |
| Sửa bệnh nhân | ✅ | `PUT /api/v1/patients/{id}` |
| Xóa bệnh nhân | ✅ | `DELETE /api/v1/patients/{id}` |
| Tìm kiếm theo từ khóa | ✅ | `GET /api/v1/patients/search?keyword=...` |
| CCCD không trùng | ✅ | [PatientServiceImpl.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/service/impl/PatientServiceImpl.java#L81-L90) — `validateUniqueCccd()` |
| Danh sách phân trang | ❌ | `getAll()` trả `List` không phân trang, đề bài yêu cầu phân trang |
| Số ĐT hợp lệ | ⚠️ | Không thấy validation regex cho phone |
| Tiền sử bệnh | ❌ | Entity Patient không có trường `medical_history` / tiền sử bệnh |

---

### ✅ F02. Quản lý bác sĩ — ĐẠT

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| CRUD bác sĩ | ✅ | `DoctorController` — 5 endpoints |
| Thông tin cá nhân, bằng cấp | ✅ | Entity Doctor có `fullName`, `licenseNumber`, `hireDate` |
| Liên kết chuyên khoa/phòng ban | ✅ | `@ManyToOne Department` |
| Mã bác sĩ duy nhất | ✅ | `license_number` unique |

> [!NOTE]
> **Lịch làm việc** (bác sĩ nghỉ/đi làm) không có entity riêng. Đề bài F03 yêu cầu "Không đặt lịch vào ngày bác sĩ nghỉ" nhưng logic này chưa implement.

---

### ✅ F03. Lịch khám — ĐẠT TỐT

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| Đặt lịch khám | ✅ | `POST /api/appointments` |
| Kiểm tra trùng lịch | ✅ | [AppointmentServiceImpl.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/service/impl/AppointmentServiceImpl.java#L55-L70) — check conflict ±29 phút |
| Check conflict nhanh (API) | ✅ | `GET /api/appointments/check-conflict` |
| Danh sách lịch theo bệnh nhân | ✅ | `GET /api/appointments/patient/{patientId}` |
| Cập nhật trạng thái | ✅ | `PUT /api/appointments/{id}/status` |
| Xuất phiếu hẹn PDF | ✅ | `GET /api/appointments/{id}/export-pdf` |
| Gửi email xác nhận | ✅ | `EmailService.sendAppointmentConfirmationEmail()` |
| Tối đa 20 lịch/ngày/bác sĩ | ❌ | Chưa implement giới hạn 20 lịch/ngày |
| Không đặt ngày bác sĩ nghỉ | ❌ | Không có bảng lịch làm việc bác sĩ |

---

### ⚠️ F04. Tiếp nhận khám — ĐẠT MỘT PHẦN

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| Check-in theo lịch hẹn | ✅ | `POST /api/v1/medical-records/check-in` |
| Tạo MedicalRecord tự động | ✅ | [MedicalRecordServiceImpl.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/service/impl/MedicalRecordServiceImpl.java#L32-L72) |
| Trạng thái Appointment → CHECKED_IN | ✅ | Có |
| Walk-in (không có lịch hẹn) | ❌ | **Chỉ accept CONFIRMED appointment**, không hỗ trợ bệnh nhân đến trực tiếp |
| Gán số thứ tự | ❌ | Không có logic gán số thứ tự chờ |
| Gán phòng khám | ⚠️ | Response có `departmentName` nhưng không gán phòng khám cụ thể |

---

### ✅ F05. Kê đơn thuốc — ĐẠT TỐT

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| Kê đơn thuốc từ danh mục | ✅ | `POST /api/v1/prescriptions` |
| Chọn thuốc theo medicine_id | ✅ | Validate thuốc tồn tại trong danh mục |
| Ghi liều dùng, cách dùng | ✅ | `dosage`, `instructions` trong PrescriptionItem |
| Snapshot giá tại thời điểm kê | ✅ | `unitPriceAtTime` — thiết kế tốt |
| Xuất đơn PDF | ✅ | `GET /api/v1/prescriptions/{id}/export-pdf` |
| Thuốc phải có trong danh mục | ✅ | Throw `ResourceNotFoundException` nếu không tìm thấy |
| Bác sĩ phải là người khám | ⚠️ | Doctor lấy từ MedicalRecord nhưng không validate rõ ràng "bác sĩ hiện tại = bác sĩ đăng nhập" |

---

### ✅ F06. Xét nghiệm — ĐẠT TỐT

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| Bác sĩ chỉ định xét nghiệm | ✅ | `POST /api/v1/lab-tests` |
| Upload file kết quả | ✅ | Multipart upload trong `PUT /{id}/result` |
| Trạng thái: ORDERED → COMPLETED | ✅ | Enum: `ORDERED, SAMPLE_COLLECTED, COMPLETED` |
| Danh sách chờ xét nghiệm | ✅ | `GET /api/v1/lab-tests/pending` |
| File ≤ 10 MB | ❌ | **Không thấy validate kích thước file upload** |
| Chỉ nhập kết quả khi "đã lấy mẫu" | ❌ | Logic hiện tại cho phép nhập kết quả khi status = ORDERED (chỉ chặn COMPLETED) — yêu cầu là chỉ nhập khi `SAMPLE_COLLECTED` |

---

### ✅ F07. Thanh toán viện phí — ĐẠT TỐT

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| Tạo hóa đơn tự động | ✅ | `POST /api/v1/invoices/medical-records/{id}` |
| Tính phí khám + thuốc + xét nghiệm | ✅ | [PaymentServiceImpl.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/service/impl/PaymentServiceImpl.java#L70-L116) — examinationFee + medicineFee + labFee |
| Bảo hiểm BHYT 80% | ✅ | `InsuranceCoverage.EIGHTY` → 80% |
| Hình thức: tiền mặt/chuyển khoản/BHYT | ✅ | Enum `PaymentMethod` |
| Xuất hóa đơn PDF | ✅ | `GET /api/v1/invoices/{id}/export` |
| Xử lý thanh toán | ✅ | `POST /api/v1/invoices/pay` |
| Lịch sử thanh toán bệnh nhân | ✅ | `GET /api/v1/invoices/patient/{patientId}` |
| Không xuất viện khi chưa thanh toán | ⚠️ | Không thấy check ràng buộc này ở tầng nghiệp vụ |

---

### ✅ F08. Báo cáo thống kê — ĐẠT TỐT

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| Báo cáo doanh thu theo khoảng ngày | ✅ | `GET /api/v1/reports/revenue?from=...&to=...` |
| Shortcut: hôm nay / tháng này | ✅ | `/revenue/today`, `/revenue/this-month` |
| Xuất Excel | ✅ | `GET /api/v1/reports/revenue/export` — Apache POI |
| Chỉ ADMIN truy cập | ✅ | `@PreAuthorize("hasRole('ADMIN')")` |
| Chi tiết từng ngày | ✅ | `DailySummary` trong response |
| Xuất PDF báo cáo | ❌ | Chỉ xuất Excel, **chưa có xuất PDF cho báo cáo** |
| Số lượt khám, hiệu suất bác sĩ | ⚠️ | Có `totalVisits` nhưng **không có báo cáo hiệu suất bác sĩ** |

---

### ✅ F09. Phân quyền người dùng — ĐẠT TỐT

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| CRUD tài khoản người dùng | ✅ | [UserController.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/controller/UserController.java) — 6 endpoints |
| Gán vai trò (4 roles) | ✅ | ADMIN, DOCTOR, NURSE, CASHIER |
| Phân quyền theo endpoint | ✅ | `@PreAuthorize` trên từng endpoint |
| Admin không tự xóa tài khoản mình | ✅ | `toggleActive()` check `getCurrentUserId()` |
| Đổi mật khẩu | ✅ | `PATCH /{id}/reset-password` |
| Mỗi user ít nhất 1 role | ⚠️ | Không validate rõ ràng trong code, phụ thuộc vào request |
| Phân trang user | ✅ | `PageRequest.of(page, size)` |

---

### ✅ F10. Nhật ký hoạt động — ĐẠT TỐT

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| Ghi log tự động (AOP) | ✅ | [AuditLogAspect.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/audit/AuditLogAspect.java) — `@Auditable` annotation |
| Ghi user, IP, thời gian | ✅ | `userId`, `ipAddress`, `createdAt` |
| Lọc theo user/ngày/loại action | ✅ | [AuditLogServiceImpl.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/service/impl/AuditLogServiceImpl.java#L34-L46) — filter đầy đủ |
| Xuất CSV | ✅ | `GET /api/audit-logs/export` |
| Chỉ đọc, không sửa/xóa | ✅ | Không có endpoint PUT/DELETE cho audit log |
| Chỉ ADMIN truy cập | ✅ | `@PreAuthorize("hasRole('ADMIN')")` ở class level |
| Lưu old_value, new_value | ⚠️ | Chỉ lưu `newValue` (kết quả trả về), **không lưu `oldValue`** trước khi thay đổi |

---

## 2. Yêu cầu Phi Chức Năng

| Tiêu chí | Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|---|
| **JWT Authentication** | JWT Auth + BCrypt | ✅ | Spring Security + JWT Filter + BCrypt |
| **Refresh Token** | — | ✅ | `POST /api/auth/refresh` |
| **Rate Limiting API** | Chống spam request | ❌ | **Hoàn toàn chưa có** |
| **HTTPS** | Bắt buộc | ⚠️ | Chưa cấu hình HTTPS (server chạy HTTP) |
| **Chống SQL Injection** | — | ✅ | Spring Data JPA + JPQL (an toàn) |
| **Chống XSS** | — | ⚠️ | Không có filter XSS rõ ràng |
| **SLF4J + Logback** | Logging | ✅ | Sử dụng `@Slf4j` khắp nơi |
| **Docker** | Đóng gói | ✅ | `Dockerfile` multi-stage + `docker-compose.yml` |
| **CI/CD (GitHub Actions)** | Auto build/test | ❌ | **Không tìm thấy `.github/workflows`** |
| **Swagger / OpenAPI** | API Docs | ✅ | SpringDoc OpenAPI tại `/swagger-ui.html` |
| **Unit Test ≥ 70%** | Code coverage | ❌ | **Chỉ có 1 file test** (`PaymentServiceImplTest.java`) |
| **JavaDoc cho public API** | — | ⚠️ | Có comment tiếng Việt nhưng **JavaDoc chưa đầy đủ** |

---

## 3. Kiến Trúc & Công Nghệ

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| Java 17 LTS | ✅ | `pom.xml`: `<java.version>17</java.version>` |
| Spring Boot 3.2.x | ✅ | Version 3.2.12 |
| Spring Security 6.x + JWT | ✅ | Có |
| Spring Data JPA / Hibernate | ✅ | Có |
| MySQL 8.x | ✅ | Docker image `mysql:8.0` |
| Maven | ✅ | `pom.xml` |
| Apache POI (Excel) | ✅ | Version 5.2.5 |
| OpenPDF (thay iText) | ✅ | `openpdf 1.3.43` |
| Email notification | ✅ | Spring Boot Mail + Thymeleaf template |
| Kiến trúc 3 tầng | ✅ | Controller → Service → Repository rõ ràng |

---

## 4. Cơ Sở Dữ Liệu

| Bảng yêu cầu | Có trong code | Entity tương ứng |
|---|---|---|
| `patients` | ✅ | [Patient.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/Patient.java) |
| `doctors` | ✅ | [Doctor.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/Doctor.java) |
| `departments` | ✅ | [Department.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/Department.java) |
| `appointments` | ✅ | [Appointment.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/Appointment.java) |
| `medical_records` | ✅ | [MedicalRecord.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/MedicalRecord.java) |
| `prescriptions` | ✅ | [Prescription.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/Prescription.java) |
| `prescription_items` | ✅ | [PrescriptionItem.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/PrescriptionItem.java) |
| `lab_tests` | ✅ | [LabTest.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/LabTest.java) |
| `medicines` | ✅ | [Medicine.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/Medicine.java) |
| `invoices` | ✅ | [Invoice.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/Invoice.java) |
| `users` | ✅ | [User.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/User.java) |
| `roles` | ✅ | [Role.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/Role.java) |
| `user_roles` | ✅ | [UserRole.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/UserRole.java) |
| `audit_logs` | ✅ | [AuditLog.java](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/java/com/hospital/entity/AuditLog.java) |
| `schema.sql` + `seed_data.sql` | ✅ | [db/](file:///c:/Users/ASUS/Downloads/SE330_Java/src/main/resources/db) |

> **Tất cả 14 bảng đề bài yêu cầu đều đã có đầy đủ.** ✅

---

## 5. Docker & Triển Khai

| Yêu cầu | Trạng thái | Chi tiết |
|---|---|---|
| `Dockerfile` | ✅ | Multi-stage build (Maven → JRE Alpine) |
| `docker-compose.yml` | ✅ | 2 services: `mysql` + `app` |
| `.env` / `.env.example` | ✅ | Có cả 2 file |
| Health check MySQL | ✅ | `mysqladmin ping` |
| Nginx reverse proxy | ❌ | Đề bài yêu cầu `be + db + nginx` nhưng chưa có nginx |
| CI/CD (GitHub Actions) | ❌ | Chưa có |

---

## 6. Tổng Kết — Danh Sách CÒN THIẾU

> [!IMPORTANT]
> ### Các mục QUAN TRỌNG cần bổ sung (ảnh hưởng điểm chức năng)

| # | Mục thiếu | Mức độ | Thuộc module |
|---|---|---|---|
| 1 | **Unit Test** — chỉ có 1 file, yêu cầu ≥ 70% coverage | 🔴 Nghiêm trọng | Kiểm thử |
| 2 | **CI/CD pipeline** (GitHub Actions) | 🔴 Nghiêm trọng | DevOps |
| 3 | **Walk-in** (tiếp nhận không lịch hẹn) | 🟠 Quan trọng | F04 |
| 4 | **Patient list phân trang** | 🟠 Quan trọng | F01 |
| 5 | **Rate limiting API** | 🟠 Quan trọng | Bảo mật |
| 6 | **LabTest: validate file ≤ 10MB** | 🟡 Trung bình | F06 |
| 7 | **LabTest: chỉ nhập kết quả khi SAMPLE_COLLECTED** | 🟡 Trung bình | F06 |
| 8 | **Giới hạn 20 lịch/ngày/bác sĩ** | 🟡 Trung bình | F03 |
| 9 | **Báo cáo hiệu suất bác sĩ** | 🟡 Trung bình | F08 |
| 10 | **Xuất PDF báo cáo** (chỉ có Excel) | 🟡 Trung bình | F08 |
| 11 | **AuditLog lưu oldValue** | 🟡 Trung bình | F10 |
| 12 | **Tiền sử bệnh trong Patient** | 🟡 Trung bình | F01 |
| 13 | **Nginx** trong docker-compose | 🟢 Nhỏ | Triển khai |
| 14 | **Validate phone hợp lệ** | 🟢 Nhỏ | F01 |

> [!WARNING]
> ### Vấn đề bảo mật cần sửa NGAY
> 
> - **JWT secret** đang hardcode trong `application.yml` — Nên dời sang biến môi trường
> - **Email password** đang commit thẳng trong `application.yml` (`msoqdlrztwyhscwg`) — **Phải xóa ngay và dùng env var**
> - CORS đang mở `*` cho mọi origin — chỉ nên dùng cho dev

---

## 7. Đánh Giá Điểm Dự Kiến

| Tiêu chí | Điểm max | Dự kiến | Ghi chú |
|---|---|---|---|
| Chức năng hoạt động đúng | 40 | **30–33** | 9/10 module chạy, F04 thiếu walk-in, thiếu một số edge case |
| Kiến trúc & Code quality | 15 | **13–14** | Phân tầng rõ, DTO pattern tốt, AOP audit |
| Bảo mật | 10 | **7–8** | JWT + RBAC tốt, thiếu rate limiting, secret hardcode |
| Kiểm thử | 10 | **2–3** | Chỉ 1 file test, rất xa so với yêu cầu 70% |
| Tài liệu | 10 | **6–7** | Có README, Swagger, nhưng thiếu báo cáo kỹ thuật, user manual |
| **Tổng (backend only)** | **85** | **~58–65** | |

> [!TIP]
> ### Ưu tiên sửa để tăng điểm nhanh nhất
> 1. **Viết Unit Test** cho các Service (PatientService, DoctorService, AppointmentService, PrescriptionService) — có thể tăng 5-7 điểm
> 2. **Thêm CI/CD** (GitHub Actions workflow cơ bản) — dễ làm, có template sẵn
> 3. **Fix walk-in** trong F04 — thêm endpoint check-in không cần appointmentId
> 4. **Thêm rate limiting** — dùng Bucket4j hoặc Spring filter đơn giản
> 5. **Chuyển secret/password ra env var** — sửa nhanh trong 5 phút
