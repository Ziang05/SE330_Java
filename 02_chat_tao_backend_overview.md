# [CHAT TASK] Tạo tài liệu tổng quan Backend hiện tại – Đồ án Java Quản lý Bệnh viện

## Mục tiêu

Đọc toàn bộ codebase backend Spring Boot và tạo ra một file Markdown tên **`BACKEND_OVERVIEW.md`** mô tả chi tiết những gì backend **đang thực sự có** tại thời điểm này.

File này sẽ được dùng bởi **frontend developer** để biết chính xác những API nào đã sẵn sàng để tích hợp.

---

## Cấu trúc yêu cầu cho file `BACKEND_OVERVIEW.md`

### Phần 1: Tổng quan kiến trúc

Mô tả:
- Tech stack (Spring Boot version, Java version, DB)
- Cấu trúc thư mục dự án (package chính)
- Các dependency quan trọng trong `pom.xml` (Spring Security, JWT, OpenPDF, Apache POI, v.v.)
- Port mặc định, base URL (`/api/v1` hoặc tương đương)

---

### Phần 2: Authentication & Authorization

Mô tả cơ chế bảo mật:
- Cách đăng nhập (endpoint, request body, response trả về token gì)
- Cách sử dụng token trong request tiếp theo (header nào, format nào)
- Danh sách **roles** hiện có và quyền tương ứng
- Các endpoint **public** (không cần token)
- Các endpoint **protected** (cần token + role cụ thể)

---

### Phần 3: Danh sách API theo module

Với **từng endpoint** đang có trong codebase, liệt kê theo bảng:

```markdown
| Method | Endpoint | Mô tả | Auth required | Role |
|--------|----------|-------|---------------|------|
| GET    | /api/patients | Lấy danh sách bệnh nhân | ✅ | ADMIN, NURSE |
```

Chia thành các section:

#### 3.1 Module Patient
#### 3.2 Module Doctor & Department  
#### 3.3 Module Appointment (Lịch khám)
#### 3.4 Module Reception (Tiếp nhận)
#### 3.5 Module Prescription (Kê đơn thuốc)
#### 3.6 Module Lab Test (Xét nghiệm)
#### 3.7 Module Payment (Thanh toán)
#### 3.8 Module Medicine (Danh mục thuốc)
#### 3.9 Module Reports (Báo cáo)
#### 3.10 Module User Management (Quản lý tài khoản)
#### 3.11 Module Audit Log (Nhật ký)
#### 3.12 Export endpoints (PDF/Excel nếu có)

---

### Phần 4: Data Models (Request/Response)

Với **từng entity chính**, mô tả các trường quan trọng:

```markdown
#### Patient
| Field | Type | Required | Ghi chú |
|-------|------|----------|---------|
| id | Long | - | Auto-generated |
| fullName | String | ✅ | |
| dateOfBirth | LocalDate | ✅ | Format: yyyy-MM-dd |
```

Ưu tiên mô tả những model mà frontend cần dùng để render form.

---

### Phần 5: Các tính năng đặc biệt

Mô tả ngắn gọn các tính năng không phải CRUD thông thường:
- **Conflict check lịch khám**: hoạt động như thế nào, trả về lỗi gì nếu trùng giờ
- **Tính BHYT**: công thức tính, các trường liên quan
- **Auto-tạo hóa đơn**: trigger khi nào, điều kiện gì
- **Email notification**: có/không, trigger khi nào
- **Export PDF/Excel**: URL endpoint, format file trả về

---

### Phần 6: Error Handling

Mô tả cấu trúc response lỗi chuẩn của hệ thống:
- Format JSON error response
- Các HTTP status code thường gặp và ý nghĩa
- Validation error format

---

### Phần 7: Những gì CHƯA có (Known gaps)

Liệt kê những task được lên kế hoạch nhưng chưa hoàn thành:

| Task ID | Mô tả | Ảnh hưởng đến Frontend |
|---------|-------|------------------------|
| T12 | Unit test PatientService, DoctorService | Không ảnh hưởng trực tiếp |
| T22 | Swagger/OpenAPI docs cho module lịch & đơn thuốc | Frontend không có docs tham khảo cho module này |
| T24 | Test edge case lịch khám & xét nghiệm | Có thể còn bug ở các trường hợp đặc biệt |
| T56 | Security test + Deployment guide | Chưa có hướng dẫn deploy |

---

## Hướng dẫn thực hiện

1. Đọc toàn bộ các file `*Controller.java` để lấy danh sách endpoint
2. Đọc `SecurityConfig.java` (hoặc tương đương) để biết endpoint nào public/protected
3. Đọc các `*Request.java`, `*Response.java`, `*DTO.java` để biết data model
4. Đọc `application.yml` để lấy port, base path
5. Đọc `pom.xml` để lấy tech stack

Tạo file `BACKEND_OVERVIEW.md` tại thư mục gốc của dự án (hoặc trong `/docs/`).

> File này phải **đủ chi tiết** để một frontend developer mới nhìn vào là biết ngay cần gọi API gì, gửi gì, nhận về gì.
