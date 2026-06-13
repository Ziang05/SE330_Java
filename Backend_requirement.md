**ĐỀ BÀI ĐỒ ÁN MÔN LẬP TRÌNH JAVA**

**PHẦN MỀM QUẢN LÝ BỆNH VIỆN**

_Hospital Management System_

Nhóm: Quý • Giang • Văn • Hưng • Vinh

_Phiên bản 1.1 — 2025_

# 1\. TỔNG QUAN DỰ ÁN

## 1.1. Mục tiêu

Xây dựng một phần mềm quản lý bệnh viện bằng Java/Spring Boot, hỗ trợ tin học hoá các quy trình cốt lõi: tiếp nhận bệnh nhân, khám chữa bệnh, kê đơn, xét nghiệm và thanh toán viện phí.

## 1.2. Đối tượng sử dụng

| Vai trò | Chức năng chính |
| --- | --- |
| Quản trị viên (Admin) | Cấu hình hệ thống, quản lý người dùng, phân quyền, xem nhật ký |
| Bác sĩ (Doctor) | Xem lịch khám, tiếp nhận bệnh nhân, kê đơn, yêu cầu xét nghiệm |
| Y tá / Điều dưỡng (Nurse) | Hỗ trợ tiếp nhận, theo dõi bệnh nhân, thực hiện xét nghiệm |
| Thu ngân (Cashier) | Lập hóa đơn, thu tiền, xử lý thanh toán viện phí (tiền mặt, chuyển khoản, BHYT) |

## 1.3. Phạm vi dự án

*   Quản lý thông tin bệnh nhân, bác sĩ, phòng ban
*   Đặt lịch và tiếp nhận khám bệnh
*   Kê đơn thuốc (từ danh mục thuốc tĩnh) và chỉ định xét nghiệm
*   Thanh toán viện phí (tiền mặt, chuyển khoản, bảo hiểm BHYT)
*   Báo cáo thống kê theo ngày/tháng/năm
*   Phân quyền người dùng và nhật ký hoạt động

## 1.4. Ràng buộc

*   Ngôn ngữ: Java 17+, framework Spring Boot 3.x
*   Cơ sở dữ liệu quan hệ: MySQL 8.x hoặc PostgreSQL 15+
*   Giao diện: React 18 hoặc Thymeleaf (nhóm tự chọn)
*   Thời gian thực hiện: 1 học kỳ (~14 tuần)
*   Nhóm 5 thành viên, mỗi thành viên chịu trách nhiệm ít nhất 1 module chính

# 2\. DANH SÁCH CHỨC NĂNG CHI TIẾT

## F01. Quản lý bệnh nhân

Mô tả: Thêm, sửa, xóa, tìm kiếm thông tin bệnh nhân (họ tên, CCCD, ngày sinh, địa chỉ, số điện thoại, nhóm máu, tiền sử bệnh).

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Form nhập liệu thông tin bệnh nhân; từ khoá tìm kiếm |
| Output | Danh sách bệnh nhân phân trang; hồ sơ bệnh nhân đầy đủ |
| Ràng buộc | CCCD không trùng; số điện thoại hợp lệ; tuổi không âm |

## F02. Quản lý bác sĩ

Mô tả: Quản lý hồ sơ bác sĩ: thông tin cá nhân, bằng cấp, chuyên khoa, lịch làm việc.

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Form thêm/sửa bác sĩ; mã chuyên khoa |
| Output | Danh sách bác sĩ theo phòng/chuyên khoa; hồ sơ chi tiết |
| Ràng buộc | Mã bác sĩ duy nhất; chuyên khoa phải thuộc danh mục hệ thống |

## F03. Lịch khám

Mô tả: Đặt lịch khám cho bệnh nhân theo bác sĩ và khung giờ. Kiểm tra trùng lịch, gửi xác nhận.

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Mã bệnh nhân, mã bác sĩ, ngày giờ mong muốn, lý do khám |
| Output | Phiếu hẹn với mã lịch; danh sách lịch theo ngày/bác sĩ |
| Ràng buộc | Không đặt lịch vào ngày bác sĩ nghỉ; tối đa 20 lịch/ngày/bác sĩ |

## F04. Tiếp nhận khám

Mô tả: Check-in bệnh nhân theo lịch hẹn hoặc khám trực tiếp (walk-in). Gán số thứ tự, phòng khám.

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Mã lịch hẹn hoặc CCCD bệnh nhân |
| Output | Phiếu khám với số thứ tự; trạng thái cập nhật (chờ/đang khám/đã khám) |
| Ràng buộc | Walk-in ưu tiên thấp hơn có hẹn; phòng khám phải còn hoạt động |

## F05. Kê đơn thuốc

Mô tả: Bác sĩ kê đơn thuốc cho bệnh nhân sau khám. Chọn thuốc từ danh mục, ghi liều dùng, cách dùng.

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Mã hồ sơ khám, danh sách thuốc (mã thuốc, số lượng, liều dùng) |
| Output | Đơn thuốc in được (PDF); bản ghi đơn thuốc lưu trong hệ thống |
| Ràng buộc | Thuốc phải có trong danh mục; bác sĩ phải là người khám |

## F06. Xét nghiệm

Mô tả: Bác sĩ chỉ định xét nghiệm, nhân viên nhập kết quả, hệ thống lưu và hiển thị kết quả.

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Mã hồ sơ khám, loại xét nghiệm, file kết quả (PDF/ảnh) |
| Output | Phiếu chỉ định xét nghiệm; kết quả xét nghiệm có thể tra cứu |
| Ràng buộc | Kết quả chỉ được nhập khi trạng thái là 'đã lấy mẫu'; file ≤ 10 MB |

## F07. Thanh toán viện phí

Mô tả: Lập hóa đơn tự động từ dịch vụ sử dụng (khám, xét nghiệm), tính phí bảo hiểm BHYT, thu tiền bệnh nhân.

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Mã hồ sơ khám; hình thức thanh toán (tiền mặt/chuyển khoản/BHYT) |
| Output | Hóa đơn xuất PDF; cập nhật trạng thái thanh toán |
| Ràng buộc | Không được xuất viện khi chưa thanh toán; giảm đúng tỉ lệ bảo hiểm BHYT |

## F08. Báo cáo thống kê

Mô tả: Báo cáo doanh thu, số lượt khám, hiệu suất bác sĩ. Xuất Excel/PDF.

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Khoảng thời gian (ngày/tháng/năm); loại báo cáo |
| Output | Biểu đồ trực quan; file Excel/PDF có thể tải về |
| Ràng buộc | Chỉ Admin xem được báo cáo tài chính; dữ liệu thời gian thực |

## F09. Phân quyền người dùng

Mô tả: Quản lý tài khoản người dùng, gán vai trò, cấu hình quyền truy cập theo endpoint.

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Thông tin tài khoản, danh sách vai trò được gán |
| Output | Tài khoản hoạt động với quyền đúng; từ chối truy cập trái phép |
| Ràng buộc | Mỗi user ít nhất 1 role; Admin không thể tự xóa tài khoản mình |

## F10. Nhật ký hoạt động

Mô tả: Ghi lại mọi thao tác quan trọng (tạo/sửa/xóa bản ghi, đăng nhập, đăng xuất) kèm thông tin người thực hiện, thời gian, địa chỉ IP.

| Thuộc tính | Chi tiết |
| --- | --- |
| Input | Tự động thu thập từ mọi request; filter theo người dùng/ngày/loại action |
| Output | Danh sách nhật ký có thể tìm kiếm và lọc; xuất CSV |
| Ràng buộc | Nhật ký chỉ được đọc, không sửa/xóa; lưu tối thiểu 90 ngày |

# 3\. YÊU CẦU PHI CHỨC NĂNG

| Tiêu chí | Yêu cầu |
| --- | --- |
| Bảo mật | JWT Authentication; HTTPS; mã hoá mật khẩu BCrypt; chống SQL Injection, XSS; rate limiting API |
| Hiệu năng | Thời gian phản hồi API ≤ 500ms cho 95% request; hỗ trợ ≥ 100 user đồng thời |
| Backup | Backup database hàng ngày (tự động); lưu trữ ≥ 7 ngày; restore có thể kiểm thử |
| Logging | Log toàn bộ error ≥ WARN bằng SLF4J/Logback; audit log riêng cho hành động quan trọng |
| Mở rộng | Kiến trúc layered rõ ràng; dễ thêm module mới; hỗ trợ horizontal scaling với Docker |
| Tương thích | Chrome, Firefox, Edge (2 phiên bản mới nhất); hỗ trợ màn hình ≥ 1280px |
| Khả dụng | Uptime ≥ 99% trong giờ hoạt động (7h-22h); downtime có thông báo |
| Bảo trì | Code coverage unit test ≥ 70%; JavaDoc cho các public API; README đầy đủ |

# 4\. KIẾN TRÚC & CÔNG NGHỆ ĐỀ XUẤT

## 4.1. Công nghệ sử dụng

| Công nghệ | Phiên bản | Mục đích |
| --- | --- | --- |
| Java | 17 LTS (OpenJDK) | Ngôn ngữ lập trình chính |
| Spring Boot | 3.2.x | Framework backend chính |
| Spring Security | 6.x + JWT | Authentication & Authorization |
| Spring Data JPA | Hibernate ORM | Tương tác cơ sở dữ liệu |
| Database | MySQL 8.x / PostgreSQL 15+ | Hệ quản trị CSDL quan hệ |
| Frontend | React 18 + Vite hoặc Thymeleaf | Giao diện người dùng |
| API | RESTful JSON API | Giao tiếp FE ↔ BE |
| Build tool | Maven hoặc Gradle | Quản lý dependency, build |
| Container | Docker + Docker Compose | Đóng gói và triển khai |
| CI/CD | GitHub Actions | Tự động build, test, deploy |
| API Docs | Swagger / OpenAPI 3 | Tài liệu API tự sinh |
| Logging | SLF4J + Logback | Logging framework |
| Testing | JUnit 5 + Mockito + Testcontainers | Framework kiểm thử |
| Export | Apache POI (Excel) + iText (PDF) | Xuất báo cáo |

## 4.2. Sơ đồ kiến trúc module

Hệ thống theo kiến trúc 3 tầng (Presentation – Business Logic – Data), triển khai trong Docker:

| Tầng | Công nghệ | Mô tả |
| --- | --- | --- |
| FRONTEND LAYER | React 18 / Thymeleaf | Giao diện người dùng, gọi REST API qua Axios/Fetch |
| API GATEWAY | Spring Boot REST Controller | Nhận request, phân luồng, trả response JSON |
| SECURITY LAYER | Spring Security + JWT Filter | Xác thực token, kiểm tra phân quyền endpoint |
| SERVICE LAYER | Business Logic Services | Xử lý nghiệp vụ: validate, tính toán, điều phối |
| REPOSITORY LAYER | Spring Data JPA Repositories | Truy vấn CSDL thông qua ORM |
| DATABASE LAYER | MySQL / PostgreSQL | Lưu trữ dữ liệu quan hệ, backup tự động |
| AUDIT/LOG LAYER | AOP + Logback | Xuyên suốt toàn hệ thống: ghi log và audit |

# 5\. THIẾT KẾ CƠ SỞ DỮ LIỆU

## 5.1. Danh sách bảng chính

| Bảng | Trường chính | Mô tả |
| --- | --- | --- |
| patients | patient_id (PK), full_name, dob, gender, cccd, phone, address, blood_type, insurance_number, created_at | Thông tin bệnh nhân |
| doctors | doctor_id (PK), full_name, phone, email, specialization_id (FK), license_number, hire_date | Thông tin bác sĩ |
| departments | dept_id (PK), dept_name, location, phone | Phòng ban / Chuyên khoa |
| appointments | appt_id (PK), patient_id (FK), doctor_id (FK), appt_datetime, status, notes, created_at | Lịch hẹn khám |
| medical_records | record_id (PK), patient_id (FK), doctor_id (FK), appt_id (FK), diagnosis, visit_date, notes | Hồ sơ khám bệnh |
| prescriptions | pres_id (PK), record_id (FK), doctor_id (FK), issued_date, status | Đơn thuốc |
| prescription_items | item_id (PK), pres_id (FK), medicine_id (FK), quantity, dosage, instructions | Chi tiết đơn thuốc |
| lab_tests | test_id (PK), record_id (FK), test_type, ordered_by (FK→doctors), result, result_file_url, status, test_date | Xét nghiệm |
| medicines | medicine_id (PK), medicine_name, generic_name, category, unit, unit_price | Danh mục thuốc (tĩnh — seed data) |
| invoices | invoice_id (PK), record_id (FK), patient_id (FK), total_amount, insurance_amount, paid_amount, payment_method, status, paid_at | Hóa đơn viện phí |
| users | user_id (PK), username, password_hash, email, is_active, doctor_id (FK nullable), created_at | Tài khoản người dùng |
| roles | role_id (PK), role_name (ADMIN/DOCTOR/NURSE/CASHIER) | Vai trò / Quyền |
| user_roles | user_id (FK), role_id (FK) — composite PK | Bảng trung gian User-Role |
| audit_logs | log_id (PK), user_id (FK), action, entity_type, entity_id, old_value, new_value, ip_address, created_at | Nhật ký hoạt động |

Lưu ý: Bảng medicines đóng vai trò danh mục tĩnh (không có logic nhập/xuất kho). Dữ liệu được khởi tạo qua seed\_data.sql.

## 5.2. Mối quan hệ chính (ERD tóm tắt)

| Bảng A | Bảng B | Quan hệ | Ghi chú |
| --- | --- | --- | --- |
| patients | appointments | 1 – N | Một bệnh nhân có nhiều lịch hẹn |
| doctors | appointments | 1 – N | Một bác sĩ nhận nhiều lịch hẹn |
| appointments | medical_records | 1 – 1 | Một lịch hẹn tạo ra tối đa 1 hồ sơ khám |
| medical_records | prescriptions | 1 – N | Một hồ sơ có nhiều đơn thuốc (tái khám) |
| prescriptions | prescription_items | 1 – N | Một đơn gồm nhiều dòng thuốc |
| prescription_items | medicines | N – 1 | Nhiều dòng đơn thuốc → 1 loại thuốc (danh mục) |
| medical_records | lab_tests | 1 – N | Một hồ sơ có nhiều phiếu xét nghiệm |
| medical_records | invoices | 1 – 1 | Một hồ sơ tạo 1 hóa đơn |
| users | user_roles | 1 – N | Một user có nhiều role |
| roles | user_roles | 1 – N | Một role gán cho nhiều user |

# 6\. GIAO DIỆN NGƯỜI DÙNG

| Màn hình | Người dùng | Chức năng chính |
| --- | --- | --- |
| Dashboard | Admin / Tất cả | Thống kê nhanh: số lịch khám hôm nay, doanh thu tuần, số bệnh nhân mới; biểu đồ mini |
| Quản lý bệnh nhân | Admin, Nurse, Doctor | CRUD bệnh nhân, tìm kiếm, xem hồ sơ đầy đủ, lịch sử khám |
| Quản lý bác sĩ | Admin | CRUD bác sĩ, phân khoa, lịch làm việc, thống kê lượt khám |
| Lịch khám | Admin, Nurse, Doctor | Calendar view, tạo/hủy/sửa lịch hẹn, xem theo ngày/tuần/tháng |
| Tiếp nhận | Nurse | Check-in bệnh nhân, gán phòng và số thứ tự, in phiếu chờ |
| Kê đơn & Xét nghiệm | Doctor | Form kê đơn thuốc (chọn từ danh mục), chỉ định xét nghiệm, xem kết quả |
| Thanh toán | Cashier | Danh sách chờ thanh toán, lập hóa đơn (tính BHYT), chọn phương thức, in hóa đơn PDF |
| Báo cáo thống kê | Admin | Báo cáo doanh thu, lượt khám; biểu đồ; xuất Excel/PDF |
| Quản trị hệ thống | Admin | Quản lý tài khoản, phân quyền, xem nhật ký hoạt động, cấu hình hệ thống |
| Đăng nhập / Hồ sơ | Tất cả | Form đăng nhập, đổi mật khẩu, cập nhật hồ sơ cá nhân |

# 7\. KẾ HOẠCH KIỂM THỬ

## 7.1. Các tầng kiểm thử

| Tầng | Công cụ | Nội dung | Tiêu chuẩn đạt |
| --- | --- | --- | --- |
| Unit Test | JUnit 5 + Mockito | Service layer: test từng method riêng lẻ, mock Repository | ≥ 70% coverage |
| Integration Test | Spring Test + Testcontainers | Test toàn bộ luồng API từ Controller → DB thực | Các luồng nghiệp vụ chính |
| Functional Test | Postman / REST Assured | Test API theo spec: happy path + edge case | 100% endpoint được test |
| Security Test | OWASP ZAP / thủ công | SQL Injection, XSS, Unauthorized access, JWT expired | Tất cả endpoint bảo mật |
| Performance Test | JMeter (optional) | Load test 100 user đồng thời, kiểm tra response time | ≤ 500ms p95 |

## 7.2. Kịch bản test chính

| Mã TC | Module | Kịch bản |
| --- | --- | --- |
| TC-01 | Đặt lịch khám | Đặt lịch thành công; đặt trùng giờ báo lỗi; đặt cho bác sĩ nghỉ báo lỗi |
| TC-02 | Tiếp nhận bệnh nhân | Check-in theo lịch; walk-in không có lịch; bệnh nhân chưa có hồ sơ |
| TC-03 | Kê đơn thuốc | Kê đơn thành công; thuốc không có trong danh mục báo lỗi; bác sĩ không phải người khám bị từ chối |
| TC-04 | Thanh toán | Tạo hóa đơn đúng tổng tiền; áp bảo hiểm BHYT đúng tỉ lệ; thanh toán thiếu tiền xử lý thế nào |
| TC-05 | Phân quyền | Cashier không xem được báo cáo tài chính; Doctor không truy cập trang quản trị |
| TC-06 | Xét nghiệm | Tạo phiếu → lấy mẫu → nhập kết quả; nhập kết quả khi chưa lấy mẫu báo lỗi |
| TC-07 | Nhật ký | Mọi CRUD đều tạo log; log hiển thị đúng user/thời gian/IP; không xóa được log |

# 8\. HƯỚNG DẪN TRIỂN KHAI

## 8.1. Build & Chạy local

*   Yêu cầu môi trường: JDK 17+, Node 18+, Docker Desktop, Maven 3.9+ hoặc Gradle 8+
*   Clone repository: git clone https://github.com/<team>/hospital-management.git
*   Cấu hình biến môi trường: tạo file .env từ .env.example, điền thông tin DB
*   Khởi động bằng Docker Compose: docker compose up -d
*   Truy cập: Backend API tại http://localhost:8080, Frontend tại http://localhost:3000
*   Swagger UI: http://localhost:8080/swagger-ui.html

## 8.2. Biến môi trường quan trọng

| Biến | Giá trị mặc định | Mô tả |
| --- | --- | --- |
| DB_HOST | localhost | Host của MySQL/PostgreSQL |
| DB_PORT | 3306 | Port cơ sở dữ liệu |
| DB_NAME | hospital_db | Tên database |
| DB_USER | hospital_user | Username DB |
| DB_PASS | *** | Mật khẩu DB (không commit lên Git) |
| JWT_SECRET | *** | Khóa ký JWT (≥ 256 bit, random) |
| JWT_EXPIRATION | 86400000 | Thời gian hết hạn token (ms) |
| SERVER_PORT | 8080 | Port backend Spring Boot |
| SPRING_PROFILES_ACTIVE | dev/prod | Profile môi trường |

# 9\. DANH SÁCH TÀI LIỆU CẦN NỘP

| STT | Tài liệu | Mô tả | Bắt buộc? |
| --- | --- | --- | --- |
| 1 | Mã nguồn | Repository Git (GitHub/GitLab) với commit history rõ ràng | Bắt buộc |
| 2 | README.md | Hướng dẫn setup, run, cấu trúc project, biến môi trường | Bắt buộc |
| 3 | Báo cáo kỹ thuật | Tài liệu Word/PDF: kiến trúc, ERD, API docs, quyết định thiết kế | Bắt buộc |
| 4 | Hướng dẫn sử dụng | User Manual PDF: hướng dẫn từng chức năng cho từng vai trò | Bắt buộc |
| 5 | Slide thuyết trình | PowerPoint/Google Slides: tổng quan, demo flow, phân công | Bắt buộc |
| 6 | Video demo | Quay màn hình 5–10 phút: demo đầy đủ các luồng chính | Bắt buộc |
| 7 | Script SQL | File schema.sql và seed_data.sql để khởi tạo DB mẫu | Bắt buộc |
| 8 | Swagger Export | File openapi.json / HTML export từ Swagger UI | Khuyến khích |
| 9 | Test Report | Báo cáo kết quả test: coverage report, Postman run result | Khuyến khích |

# 10\. TIÊU CHÍ CHẤM ĐIỂM

| Tiêu chí | Nội dung đánh giá | Điểm tối đa |
| --- | --- | --- |
| Chức năng hoạt động đúng | Tất cả 10 chức năng chính chạy đúng, không crash, xử lý edge case | 40 |
| Kiến trúc & Code quality | Phân tầng rõ ràng, tuân thủ SOLID, không có code smell nghiêm trọng | 15 |
| Bảo mật | JWT hoạt động, phân quyền đúng theo role, log audit đầy đủ | 10 |
| Giao diện người dùng | UI đủ màn hình, responsive, UX hợp lý, không lỗi hiển thị | 10 |
| Kiểm thử | Coverage ≥ 70%, integration test đủ luồng, test report rõ ràng | 10 |
| Tài liệu | README, báo cáo, user manual, slide đầy đủ và chất lượng | 10 |
| Demo & Thuyết trình | Demo trôi chảy, trả lời câu hỏi tốt, phân công rõ ràng | 5 |
| Tổng |  | 100 |

# 11\. DANH SÁCH TASK CHI TIẾT

Tổng cộng 56 task, phân bổ đều cho 5 thành viên.

| Thành viên (Task range) | Module chính | Số task | Vai trò |
| --- | --- | --- | --- |
| Hưng (T01–T12) | Kiến trúc, ERD, API Patient & Doctor | 12 task | Team Lead |
| Vinh (T13–T24) | API Lịch khám, Tiếp nhận, Kê đơn, Xét nghiệm | 12 task | Backend Dev |
| Quý (T25–T36) | Toàn bộ Frontend, tích hợp API | 12 task | Frontend Dev |
| Văn (T37–T46) | API Thanh toán, Báo cáo, Danh mục thuốc | 10 task | Backend + Data |
| Giang (T47–T56) | Security, JWT, Audit Log, Docker, CI/CD | 10 task | DevOps / Security |

**Ghi chú về phụ thuộc task:**

*   Nhóm Infra (T01–T04) phải hoàn thành trước khi bắt đầu phát triển feature
*   ERD (T02) phải done trước khi viết Entity/Repository
*   Backend API phải stable trước khi Frontend tích hợp
*   Security (T47–T49) nên integrate sớm để tránh refactor lớn ở cuối

# 12\. BẢNG PHÂN CÔNG CÔNG VIỆC CHI TIẾT

## Quý – Team Lead / Backend Core

Vai trò: Chịu trách nhiệm kiến trúc tổng thể, module Patient & Doctor, review code toàn team

Danh sách task: T01 (Kiến trúc), T02 (ERD), T03 (SQL schema), T04 (Spring Boot setup), T05–T07 (Entity/Service/API Patient), T08–T09 (Entity/Service/API Doctor), T10 (Validation/Search), T11 (Unit test), T12 (README + Review)

**Đầu ra kỳ vọng:**

*   REST API đầy đủ: GET/POST/PUT/DELETE cho Patient và Doctor
*   ERD hoàn chỉnh với tất cả quan hệ và ràng buộc
*   File schema.sql, seed\_data.sql (bao gồm danh mục thuốc) và README kỹ thuật
*   Unit test cho PatientService và DoctorService đạt ≥ 80% coverage
*   Code review ít nhất 2 PR/tuần

## Vinh – Backend Feature (Lịch khám & Đơn thuốc)

Vai trò: Phát triển module lịch hẹn, tiếp nhận, kê đơn, xét nghiệm

Danh sách task: T13 (Entity), T14 (API Lịch hẹn), T15 (Conflict check), T16 (API Tiếp nhận), T17 (API Kê đơn), T18 (API Xét nghiệm), T19 (Email notify), T20–T21 (Unit/Integration test), T22 (API docs), T23 (Export PDF đơn thuốc), T24 (Edge case test)

**Đầu ra kỳ vọng:**

*   API Appointment CRUD + conflict detection hoạt động đúng
*   API Prescription có thể tạo, thêm thuốc từ danh mục, in PDF
*   API Lab Test hỗ trợ upload kết quả file
*   Tài liệu Swagger cho tất cả endpoint của module
*   Integration test luồng: đặt lịch → tiếp nhận → kê đơn

## Quý – Frontend Developer

Vai trò: Xây dựng toàn bộ giao diện web, tích hợp API backend, UX/UI cho 10 màn hình chính

Danh sách task: T25 (Setup FE), T26 (Layout), T27 (Dashboard), T28 (Patient UI), T29 (Doctor UI), T30 (Appointment UI), T31 (Reception UI), T32 (Prescription UI), T33 (Payment UI), T34 (Report UI), T35 (API integration), T36 (Error handling + Responsive)

**Đầu ra kỳ vọng:**

*   10 màn hình hoạt động đầy đủ, kết nối API backend
*   Form validation phía frontend trước khi gửi request
*   Dashboard với biểu đồ thống kê
*   Responsive design trên màn hình ≥ 1280px
*   Toast notification, loading state, error handling nhất quán

## Văn – Backend Finance & Data

Vai trò: Module thanh toán viện phí, danh mục thuốc (tĩnh), báo cáo thống kê và xuất file

Danh sách task: T37 (Entity Invoice), T38 (API Thanh toán + tính BHYT), T39 (API Danh mục thuốc — CRUD đơn giản), T40 (API Báo cáo), T41 (Export Excel báo cáo), T42 (Export PDF hóa đơn), T43–T44 (Test), T45 (User Manual), T46 (Edge case test)

**Đầu ra kỳ vọng:**

*   API Payment: tạo hóa đơn, tính bảo hiểm BHYT, cập nhật trạng thái
*   API Medicines: CRUD danh mục thuốc (không cần logic kho)
*   API Reports: doanh thu, lượt khám theo ngày/tháng/năm
*   Xuất báo cáo Excel và hóa đơn PDF hoạt động đúng
*   User Manual module tài chính

## Giang – DevOps / Security

Vai trò: Bảo mật hệ thống, phân quyền RBAC, audit log, Docker, CI/CD và kiểm thử bảo mật

Danh sách task: T47 (JWT config), T48 (RBAC 4 roles), T49 (Phân quyền endpoint), T50 (API User/Role), T51 (Audit Log), T52 (API nhật ký), T53 (Docker), T54 (Env vars), T55 (CI/CD), T56 (Security test + Deployment guide)

**Đầu ra kỳ vọng:**

*   Spring Security + JWT: login/logout/refresh token hoạt động
*   RBAC: 4 role (Admin, Doctor, Nurse, Cashier) với quyền đúng theo đặc tả
*   Audit Log ghi đầy đủ mọi action quan trọng
*   docker-compose.yml chạy được toàn bộ stack (be + db + nginx)
*   GitHub Actions pipeline: build → test → docker build tự động

# 13\. GHI CHÚ & KHUYẾN NGHỊ

## 13.1. Quy trình làm việc nhóm đề xuất

*   Sử dụng Git Flow: branch main (production), develop (integration), feature/\* (từng tính năng)
*   Mỗi tính năng tạo một Pull Request riêng, cần ít nhất 1 người review trước khi merge
*   Họp nhóm ít nhất 1 lần/tuần để sync tiến độ và giải quyết blocker
*   Sử dụng GitHub Issues hoặc Trello để track task và bug
*   Commit message theo format: feat/fix/docs/test: mô tả ngắn

## 13.2. Thứ tự ưu tiên phát triển

*   Sprint 1 (tuần 1-2): Kiến trúc, ERD, setup project, Spring Security cơ bản
*   Sprint 2 (tuần 3-5): API Patient, Doctor, Appointment; Frontend layout + Dashboard
*   Sprint 3 (tuần 6-8): API Reception, Prescription, Lab Test; Frontend các màn hình chính
*   Sprint 4 (tuần 9-11): API Payment (tính BHYT), Danh mục thuốc, Report; Frontend tích hợp đầy đủ
*   Sprint 5 (tuần 12-13): Audit Log, Docker, CI/CD; Unit test + Integration test
*   Sprint 6 (tuần 14): Bug fix, polish UI, hoàn thiện tài liệu, chuẩn bị demo

## 13.3. Lưu ý về scope đã điều chỉnh

So với đề bài gốc, nhóm đã bỏ module F08 (Kho dược phẩm) và vai trò Dược sĩ (Pharmacist) để tập trung vào các luồng nghiệp vụ cốt lõi. Bảng medicines vẫn được giữ lại như một danh mục tĩnh phục vụ chức năng kê đơn của bác sĩ, được khởi tạo qua seed\_data.sql. Quyết định này được ghi nhận rõ trong báo cáo kỹ thuật.

## 13.4. Tài liệu tham khảo

*   Spring Boot Documentation: https://spring.io/projects/spring-boot
*   Spring Security Reference: https://spring.io/projects/spring-security
*   Swagger / OpenAPI 3: https://swagger.io/specification/
*   Docker Documentation: https://docs.docker.com
*   GitHub Actions Docs: https://docs.github.com/en/actions
*   OWASP Top 10: https://owasp.org/www-project-top-ten/

_Đồ án Java – Quản lý Bệnh viện | Phiên bản 1.1 (bỏ Pharmacist/Kho dược)_