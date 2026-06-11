# 📘 Finance Module – User Manual

> **Tác giả:** Finance Team  
> **Phiên bản:** 1.0.0  
> **Cập nhật lần cuối:** 2026-06-11  
> **Phạm vi:** Module Thanh Toán & Báo Cáo Doanh Thu (Task T38–T46)

---

## 1. Giới Thiệu

Module Finance quản lý toàn bộ luồng thanh toán và báo cáo tài chính của hệ thống bệnh viện. Module này bao gồm các chức năng sau:

| Chức năng | Mô tả |
|:---|:---|
| **Tạo hóa đơn** | Tự động sinh hóa đơn khi bác sĩ hoàn thành khám |
| **Xác nhận thanh toán** | Thu ngân xác nhận bệnh nhân đã trả tiền |
| **Tra cứu hóa đơn** | Xem chi tiết / lịch sử hóa đơn theo bệnh nhân |
| **Báo cáo doanh thu** | Admin xem tổng doanh thu theo khoảng ngày |
| **Xuất Excel** | Tải báo cáo doanh thu ra file `.xlsx` |
| **Xuất PDF** | In biên lai thanh toán ra file `.pdf` |

---

## 2. Quyền Truy Cập (Role-Based Access)

Theo `Rule.md`, mọi API đều được bảo vệ bằng `@PreAuthorize`. Bảng phân quyền:

| API | ADMIN | NURSE | DOCTOR |
|:---|:---:|:---:|:---:|
| Tạo hóa đơn | ✅ | ✅ | ✅ |
| Xác nhận thanh toán | ✅ | ✅ | ❌ |
| Xem chi tiết hóa đơn | ✅ | ✅ | ✅ |
| Xem lịch sử theo bệnh nhân | ✅ | ✅ | ✅ |
| Xuất PDF biên lai | ✅ | ✅ | ❌ |
| Báo cáo doanh thu | ✅ | ❌ | ❌ |
| Xuất Excel doanh thu | ✅ | ❌ | ❌ |

---

## 3. Cách Lấy Token Xác Thực

Tất cả API yêu cầu **JWT Bearer Token**. Cách lấy:

### Bước 1: Đăng nhập
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123456"
}
```

### Bước 2: Copy token từ response
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "role": "ADMIN"
  }
}
```

### Bước 3: Dán vào Swagger
1. Mở `http://localhost:8080/swagger-ui.html`
2. Bấm nút **Authorize 🔒**
3. Nhập: `eyJhbGciOiJIUzI1NiJ9...`
4. Bấm **Authorize** → **Close**

> **Tài khoản test có sẵn:**
> | Username | Password | Role |
> |:---|:---|:---|
> | `admin` | `Admin@123456` | ADMIN |
> | `nurse1` | `Nurse@123456` | NURSE |
> | `doctor1` | `Doctor@123456` | DOCTOR |

---

## 4. Danh Sách API

### Base URL
```
http://localhost:8080/api/v1
```

---

### 4.1. Tạo Hóa Đơn Tự Động (T40)

> Gọi sau khi bác sĩ hoàn thành hồ sơ khám. Hệ thống tự tính phí khám + thuốc + xét nghiệm, áp dụng BHYT và tạo hóa đơn ở trạng thái **PENDING**.

```http
POST /api/v1/invoices/medical-records/{medicalRecordId}
Authorization: Bearer {token}
```

**Ví dụ:**
```http
POST /api/v1/invoices/medical-records/1
```

**Response thành công (201 Created):**
```json
{
  "success": true,
  "message": "Tao hoa don thanh cong",
  "data": {
    "id": 1,
    "medicalRecordId": 1,
    "patientId": 1,
    "patientName": "Test Patient",
    "examinationFee": 150000,
    "medicineFee": 62000,
    "labFee": 230000,
    "totalAmount": 442000,
    "insuranceCoverage": "EIGHTY",
    "insuranceAmount": 353600,
    "paidAmount": 88400,
    "paymentMethod": null,
    "status": "PENDING",
    "paidAt": null,
    "createdAt": "2026-06-11T21:00:00"
  }
}
```

**Công thức tính:**
```
totalAmount    = examinationFee (150,000đ cố định) + medicineFee + labFee
insuranceAmount = totalAmount × 80%  (nếu bệnh nhân có BHYT)
               = 0                   (nếu không có BHYT)
paidAmount     = totalAmount - insuranceAmount
```

**Lỗi thường gặp:**
| HTTP | Nguyên nhân |
|:---:|:---|
| `400` | Hóa đơn đã tồn tại cho hồ sơ khám này |
| `404` | MedicalRecord không tồn tại |
| `403` | Không đủ quyền |

---

### 4.2. Xác Nhận Thanh Toán (T39)

> Thu ngân xác nhận bệnh nhân đã trả tiền. Chuyển hóa đơn từ **PENDING → PAID**.

```http
POST /api/v1/invoices/pay
Authorization: Bearer {token}
Content-Type: application/json

{
  "invoiceId": 1,
  "paymentMethod": "CASH"
}
```

**Giá trị hợp lệ cho `paymentMethod`:**
| Giá trị | Ý nghĩa |
|:---:|:---|
| `CASH` | Tiền mặt |
| `TRANSFER` | Chuyển khoản |
| `CREDIT_CARD` | Thẻ tín dụng |

**Response thành công (200 OK):**
```json
{
  "success": true,
  "message": "Thanh toan thanh cong",
  "data": {
    "id": 1,
    "status": "PAID",
    "paymentMethod": "CASH",
    "paidAt": "2026-06-11T21:33:00",
    ...
  }
}
```

**Lỗi thường gặp:**
| HTTP | Nguyên nhân |
|:---:|:---|
| `400` | Hóa đơn đã được thanh toán rồi |
| `400` | Hóa đơn đã bị hủy, không thể thanh toán |
| `404` | Hóa đơn không tồn tại |

---

### 4.3. Xem Chi Tiết Một Hóa Đơn

```http
GET /api/v1/invoices/{id}
Authorization: Bearer {token}
```

**Ví dụ:** `GET /api/v1/invoices/1`

---

### 4.4. Xem Lịch Sử Hóa Đơn Theo Bệnh Nhân

```http
GET /api/v1/invoices/patient/{patientId}
Authorization: Bearer {token}
```

**Ví dụ:** `GET /api/v1/invoices/patient/1`

Kết quả trả về **danh sách** hóa đơn của bệnh nhân, sắp xếp **mới nhất trước**.

---

### 4.5. Xuất Hóa Đơn PDF (T44)

> Tạo file biên lai thanh toán dạng PDF để in cho bệnh nhân.

```http
GET /api/v1/invoices/{id}/export
Authorization: Bearer {token}
```

**Ví dụ:** `GET /api/v1/invoices/1/export`

- **Response:** File `invoice-1.pdf` tự động download
- **Yêu cầu:** Hóa đơn phải ở trạng thái bất kỳ (kể cả PENDING)
- **Quyền:** `ADMIN` hoặc `NURSE`

**Cách test trên Swagger:**
1. Gọi API → Swagger sẽ hiển thị nút **Download file**
2. Bấm Download → mở file PDF bằng trình đọc PDF

---

### 4.6. Xem Báo Cáo Doanh Thu (T42)

> Dành cho Admin xem tổng doanh thu trong khoảng thời gian tùy chọn.

```http
GET /api/v1/reports/revenue?from=YYYY-MM-DD&to=YYYY-MM-DD
Authorization: Bearer {token}
```

**Ví dụ:** `GET /api/v1/reports/revenue?from=2026-06-01&to=2026-06-11`

**Tham số (tùy chọn):**
| Tham số | Kiểu | Mặc định | Mô tả |
|:---|:---:|:---|:---|
| `from` | `YYYY-MM-DD` | 30 ngày trước `to` | Ngày bắt đầu |
| `to` | `YYYY-MM-DD` | Hôm nay | Ngày kết thúc |

> Nếu không truyền tham số, API tự động lấy **30 ngày gần nhất**.

**Response:**
```json
{
  "success": true,
  "data": {
    "fromDate": "2026-06-01",
    "toDate": "2026-06-11",
    "totalRevenue": 338400,
    "totalInsuranceAmount": 353600,
    "totalVisits": 2,
    "dailyBreakdown": [
      { "date": "2026-06-01", "revenue": 250000, "visits": 1 },
      { "date": "2026-06-02", "revenue": 88400,  "visits": 1 }
    ]
  }
}
```

**API rút gọn:**
```http
GET /api/v1/reports/revenue/today       # Hôm nay
GET /api/v1/reports/revenue/this-month  # Tháng này
```

---

### 4.7. Xuất Báo Cáo Doanh Thu Excel (T43)

> Tải file Excel báo cáo doanh thu gồm 2 sheet: **Summary** và **Daily Breakdown**.

```http
GET /api/v1/reports/revenue/export?from=YYYY-MM-DD&to=YYYY-MM-DD
Authorization: Bearer {token}
```

**Ví dụ:** `GET /api/v1/reports/revenue/export?from=2026-06-01&to=2026-06-11`

- **Response:** File `revenue-report-2026-06-01-to-2026-06-11.xlsx` tự động download
- **Quyền:** Chỉ `ADMIN`

---

## 5. Tích Hợp Với Các Module Khác

### Luồng tích hợp với Module Appointment

```
[Appointment Module] Bác sĩ hoàn thành khám → MedicalRecord status = "COMPLETED"
   ↓
[Finance Module] POST /api/v1/invoices/medical-records/{medicalRecordId}
   ↓
   Hóa đơn PENDING được tạo tự động
   ↓
[Thu ngân] POST /api/v1/invoices/pay   ← Bệnh nhân ra quầy trả tiền
   ↓
   Hóa đơn PAID
   ↓
[Finance Module] GET /api/v1/invoices/{id}/export ← In biên lai PDF
```

> **Lưu ý cho Appointment Module:** Sau khi bác sĩ kết thúc khám và cập nhật `MedicalRecord` thành `COMPLETED`, hãy gọi thêm `POST /api/v1/invoices/medical-records/{id}` để tự động sinh hóa đơn cho bệnh nhân.

---

## 6. Mã Lỗi Chung

| HTTP Code | Ý nghĩa | Ví dụ |
|:---:|:---|:---|
| `200` | Thành công | |
| `201` | Tạo mới thành công | Tạo hóa đơn |
| `400` | Lỗi nghiệp vụ | Hóa đơn đã tồn tại, trạng thái không hợp lệ |
| `401` | Chưa đăng nhập | Thiếu hoặc sai token |
| `403` | Không đủ quyền | NURSE gọi API báo cáo của ADMIN |
| `404` | Không tìm thấy | Invoice ID / MedicalRecord ID không tồn tại |

**Cấu trúc response lỗi:**
```json
{
  "success": false,
  "message": "Hóa đơn đã tồn tại cho hồ sơ khám này (Invoice ID: 1)",
  "timestamp": "2026-06-11T21:00:00"
}
```

---

## 7. Dữ Liệu Test Có Sẵn (Seed Data)

Sau khi chạy Docker và khởi động ứng dụng, các dữ liệu mẫu sau được tự động nạp:

| ID | Loại | Mô tả |
|:---:|:---:|:---|
| 1 | Patient | Test Patient – có BHYT (insurance_number: DN123456789) |
| 2 | Patient | Nguyen Van B – không có BHYT |
| 3 | Patient | Tran Thi C – không có BHYT |
| 1 | MedicalRecord | Viêm họng hạt, ngày hôm nay |
| 1 | Invoice | PAID, 88,400đ (có BHYT 80%) |
| 2 | Invoice | PAID, 250,000đ (không BHYT) |
| 3 | Invoice | PAID, 0đ (BHYT 100%) |

---

## 8. Khởi Động Môi Trường

```bash
# 1. Khởi động database MySQL bằng Docker
docker compose up -d

# 2. Chờ ~15 giây, sau đó chạy Spring Boot trong IntelliJ (bấm ▶)

# 3. Mở Swagger UI
http://localhost:8080/swagger-ui.html

# Nếu cần reset database hoàn toàn (xóa dữ liệu cũ)
docker compose down -v
docker compose up -d
```

> ⚠️ **Cảnh báo:** `docker compose down -v` sẽ xóa toàn bộ dữ liệu. Chỉ dùng khi dev, không dùng trên môi trường production.
