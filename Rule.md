# 🏥 Hospital Management System - Coding Conventions & Rules

Tài liệu này quy định các chuẩn mực chung về code, cấu trúc project và workflow dành cho dự án **Hospital Management System**. Mọi thành viên cần tuân thủ nghiêm ngặt để đảm bảo code sạch, dễ bảo trì và thống nhất.

---

## 📁 1. Cấu Trúc Package (Package Structure)

Phân bổ code theo đúng các package sau đây:

| Package | Mô tả & Chức năng |
| :--- | :--- |
| `entity` | Khai báo các JPA Entity. Các enum được đặt trong `entity/enums`. |
| `repository` | Tạo các interface (ví dụ: `XxxRepository`) kế thừa `JpaRepository`. |
| `dto.request` | Class nhận dữ liệu từ client (yêu cầu có các Annotation Validation). |
| `dto.response` | Class định dạng dữ liệu để trả về cho client. |
| `service` | Nơi khai báo các interface nghiệp vụ. |
| `service.impl` | Nơi implement logic nghiệp vụ, transaction, validate, gọi đến các repository. |
| `controller` | Khai báo REST API, **chỉ nhận request**, gọi service và trả về `ApiResponse`. |
| `exception` | Nơi định nghĩa custom exception và xử lý lỗi chung (`GlobalExceptionHandler`). |
| `audit` | Nơi chứa các annotation/AOP phục vụ cho việc audit log. |
| `config` | Cấu hình cho Spring, Swagger, Security,... |
| `util` | Các mapper, helper functions được sử dụng chung trong toàn hệ thống. |

---

## 🛠️ 2. Convention Chung (General Conventions)

### 2.1. API Response
- Luôn wrap dữ liệu trả về bằng `ApiResponse<T>`. 
- **Tuyệt đối không** trả Entity hay kiểu `String` trực tiếp từ Controller.
- Controller chỉ viết happy path (luồng chạy thành công), **không** dùng `try-catch` trong Controller.

### 2.2. DTO (Data Transfer Object)
- **Dữ liệu đầu vào**: Nhận thông qua các class `XxxRequest`.
- **Dữ liệu đầu ra**: Trả về thông qua các class `XxxResponse`.
- **Tuyệt đối không** bao giờ trả Entity trực tiếp ra API để tránh lộ cấu trúc database.

### 2.3. Quản Lý Exception
Sử dụng chuẩn exception để đồng nhất mã lỗi trả về:

- **Không tìm thấy dữ liệu**: 
  ```java
  throw new ResourceNotFoundException("Patient", "id", id);
  ```
- **Trùng dữ liệu**: 
  ```java
  throw new DuplicateResourceException("CCCD đã tồn tại");
  ```
- **Lỗi logic nghiệp vụ**: 
  ```java
  throw new BusinessException("Lịch đã hủy, không thể check-in");
  ```
- **Không dùng**: `throw new RuntimeException(...)` trong code nghiệp vụ.

> [!NOTE]
> Nếu cần thêm custom exception mới, hãy tạo class kế thừa `RuntimeException`, thêm `@ExceptionHandler` tương ứng vào `GlobalExceptionHandler`, và thông báo cho toàn bộ nhóm.

### 2.4. Validation
- Khai báo các annotation như `@NotBlank`, `@Size`, `@Pattern` trực tiếp trên DTO.
- **Không** viết code validate thủ công (if-else) các field trong Service.
- Trong Controller, bắt buộc phải có annotation `@Valid` đặt trước `@RequestBody`.

### 2.5. Audit Log
- Các phương thức quan trọng có tác động thay đổi dữ liệu (`CREATE`, `UPDATE`, `DELETE`) phải gắn annotation `@Auditable`.
- **Ví dụ**:
  ```java
  @Auditable(action = "CREATE", entityType = "Patient")
  ```

### 2.6. Service Layer
- Mỗi Service **phải** có `interface` và class `impl` tương ứng. Không viết code trực tiếp vào một class `@Service`.
- **Quy tắc chặn luồng**: Không gọi `Repository` trực tiếp từ `Controller`.

### 2.7. Logging
- Sử dụng annotation `@Slf4j` kết hợp với `log.info` hoặc `log.error`.
- **Tuyệt đối không** dùng `System.out.println` trong code.

### 2.8. Quy Chuẩn Git
- **Tên Branch**: 
  - Tính năng mới: `feature/ten-tinh-nang`
  - Sửa lỗi: `fix/mo-ta-bug`
- **Tiền tố Commit Message**: 
  Sử dụng quy chuẩn Conventional Commits: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`
- **Bảo mật**: Không bao giờ commit file `.env` lên repository.

### 2.9. Thay Đổi Database Schema
- Mọi thao tác sửa đổi Entity **phải** được thông báo cho toàn nhóm.
- **Cách cập nhật lại database**:
  ```bash
  docker compose down -v
  docker compose up -d --build
  ```

> [!WARNING]
> Lệnh `down -v` sẽ xóa toàn bộ volume và dữ liệu hiện có. **Chỉ dùng khi dev**.
