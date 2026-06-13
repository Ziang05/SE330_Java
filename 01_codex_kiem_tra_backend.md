# [CODEX TASK] Kiểm tra tiến độ hoàn thành Backend – Đồ án Java Quản lý Bệnh viện

> **Lưu ý quan trọng:** Đây là nhiệm vụ **CHỈ ĐỌC (read-only)**. Tuyệt đối **KHÔNG** thay đổi, sửa, thêm, xóa bất kỳ dòng code nào. Mục tiêu duy nhất là kiểm tra và báo cáo hiện trạng.

---

## Bối cảnh dự án

Đây là dự án **Spring Boot REST API** cho hệ thống quản lý bệnh viện. Backend được chia thành các module sau:

| Module | Thành viên phụ trách | Mô tả |
|---|---|---|
| Patient & Doctor (Core) | Hưng | CRUD bệnh nhân, bác sĩ, phân khoa |
| Appointment & Prescription | Vinh | Lịch khám, tiếp nhận, đơn thuốc, xét nghiệm |
| Payment & Reports | Văn | Thanh toán BHYT, báo cáo, danh mục thuốc |
| Security & Infra | Giang | JWT, RBAC, Audit Log, Docker, CI/CD |

---

## Danh sách task backend theo file phân công

### 🔵 HƯNG – Backend Core

| Task ID | Mô tả | Trạng thái (file Excel) |
|---|---|---|
| T01 | Thiết kế kiến trúc hệ thống | ✅ Hoàn thành |
| T02 | Thiết kế ERD | ✅ Hoàn thành |
| T03 | Script SQL khởi tạo DB (schema.sql + seed_data.sql) | ✅ Hoàn thành |
| T04 | Cấu hình Spring Boot (pom.xml, application.yml) | ✅ Hoàn thành |
| T05 | Entity & Repository: Patient, Doctor, Department | ✅ Hoàn thành |
| T06 | Service & REST API: CRUD Bệnh nhân | ✅ Hoàn thành |
| T07 | Service & REST API: CRUD Bác sĩ & Phân khoa | ✅ Hoàn thành |
| T08 | Validation & Exception Handler toàn cục | ✅ Hoàn thành |
| T09 | Tìm kiếm & lọc bệnh nhân | ✅ Hoàn thành |
| T10 | README.md kỹ thuật | ✅ Hoàn thành |
| T11 | Code review toàn team | ✅ Hoàn thành |
| **T12** | **Unit test PatientService, DoctorService (JUnit 5 + Mockito)** | ❌ **Chưa** |

### 🟢 VINH – Backend Feature (Appointment & Prescription)

| Task ID | Mô tả | Trạng thái (file Excel) |
|---|---|---|
| T13 | Entity & Repository: Appointment, Reception, Prescription, LabTest | ✅ Hoàn thành |
| T14 | API đặt lịch khám | ✅ Hoàn thành |
| T15 | Kiểm tra conflict lịch khám | ✅ Hoàn thành |
| T16 | API tiếp nhận bệnh nhân | ✅ Hoàn thành |
| T17 | API kê đơn thuốc | ✅ Hoàn thành |
| T18 | API quản lý xét nghiệm | ✅ Hoàn thành |
| T19 | Email notification (optional) | ✅ Hoàn thành |
| T20 | Unit test AppointmentService, PrescriptionService | ✅ Hoàn thành |
| T21 | Integration test luồng đặt lịch → tiếp nhận → kê đơn | ✅ Hoàn thành |
| **T22** | **Tài liệu API Swagger/OpenAPI cho module lịch & đơn thuốc** | ❌ **Chưa** |
| T23 | Export lịch khám PDF / in phiếu hẹn | ✅ Hoàn thành |
| **T24** | **Test edge case: đặt lịch vượt slot, xét nghiệm thiếu kết quả** | ❌ **Chưa** |

### 🟠 VĂN – Backend Finance (Payment & Reports)

| Task ID | Mô tả | Trạng thái (file Excel) |
|---|---|---|
| T38 | Entity & Repository: Invoice, Payment, Medicine | ✅ Hoàn thành |
| T39 | API thanh toán (tạo hóa đơn, tính BHYT) | ✅ Hoàn thành |
| T40 | Tích hợp Payment ↔ Appointment | ✅ Hoàn thành |
| T41 | API danh mục thuốc CRUD | ✅ Hoàn thành |
| T42 | API báo cáo doanh thu | ✅ Hoàn thành |
| T43 | Export báo cáo Excel (Apache POI) | ✅ Hoàn thành |
| T44 | Export hóa đơn PDF (OpenPDF) | ✅ Hoàn thành |
| T45 | Unit & Integration test PaymentService | ✅ Hoàn thành |
| T46 | User Manual module tài chính | ✅ Hoàn thành |

### 🔴 GIANG – Security & Infrastructure

| Task ID | Mô tả | Trạng thái (file Excel) |
|---|---|---|
| T47 | Spring Security + JWT (login, logout, refresh token) | ✅ Hoàn thành |
| T48 | RBAC: roles ADMIN, DOCTOR, NURSE, CASHIER | ✅ Hoàn thành |
| T49 | Phân quyền endpoint (@PreAuthorize) | ✅ Hoàn thành |
| T50 | API quản lý user & phân quyền | ✅ Hoàn thành |
| T51 | Audit Log module | ✅ Hoàn thành |
| T52 | API xem nhật ký hoạt động | ✅ Hoàn thành |
| T53 | Dockerfile + docker-compose.yml | ✅ Hoàn thành |
| T54 | Cấu hình biến môi trường (.env, application-prod.yml) | ✅ Hoàn thành |
| T55 | CI/CD (GitHub Actions) | ✅ Hoàn thành |
| **T56** | **Security test: unauthorized access, SQL injection, XSS + deployment guide** | ❌ **Chưa** |

---

## Nhiệm vụ Codex cần thực hiện (CHỈ ĐỌC)

Với **từng task được đánh dấu ✅ Hoàn thành** trong bảng trên, hãy xác minh bằng cách kiểm tra codebase:

### 1. Kiểm tra sự tồn tại của file/class

Với mỗi task, tìm các file liên quan và báo cáo chúng **có tồn tại hay không**. Ví dụ:
- T05 → tìm `PatientEntity.java`, `DoctorEntity.java`, `DepartmentEntity.java`, `PatientRepository.java`, v.v.
- T06 → tìm `PatientService.java`, `PatientController.java` và các endpoint GET/POST/PUT/DELETE
- T47 → tìm cấu hình `SecurityConfig.java`, `JwtFilter.java` hoặc tương đương
- T53 → tìm `Dockerfile`, `docker-compose.yml`
- T55 → tìm `.github/workflows/*.yml`

### 2. Kiểm tra nội dung cơ bản

Với mỗi file tìm thấy, xác nhận nhanh nó có **nội dung thực sự** hay chỉ là file trống/placeholder.

### 3. Kiểm tra đặc biệt với các task CÒN THIẾU

| Task | Kiểm tra |
|---|---|
| T12 | Tìm test class cho `PatientService` và `DoctorService` trong thư mục `src/test/`. Có file không? Nếu có, có @Test method nào không? |
| T22 | Tìm cấu hình Swagger/OpenAPI (`springdoc`, `springfox`, `@OpenAPIDefinition`, `swagger-ui`). Có trong `pom.xml` không? Có `@Operation`/`@ApiResponse` annotation không? |
| T24 | Tìm test method nào kiểm tra trường hợp đặt lịch vượt slot hoặc xét nghiệm thiếu kết quả. |
| T56 | Tìm security test class hoặc file test integration liên quan đến unauthorized access, SQL injection, XSS. Tìm file `DEPLOYMENT.md` hoặc `deployment-guide.md`. |

---

## Định dạng báo cáo yêu cầu

Sau khi kiểm tra xong, hãy tạo một báo cáo theo cấu trúc sau:

```
## BÁO CÁO KIỂM TRA BACKEND

### Tổng quan
- Tổng task backend cần kiểm tra: 31
- Đã xác nhận có code thực sự: X
- Khai báo hoàn thành nhưng KHÔNG tìm thấy code: X  
- Task chưa hoàn thành (như đã biết): 4 (T12, T22, T24, T56)

### Chi tiết từng task
[Liệt kê từng task, file tìm thấy, kết quả kiểm tra]

### Cảnh báo / Bất thường
[Nếu có task được đánh dấu hoàn thành nhưng không tìm thấy code]
```

**Nhắc lại: KHÔNG chỉnh sửa bất kỳ file nào trong quá trình kiểm tra.**
