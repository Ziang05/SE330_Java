-- ============================================================================
-- SEED DATA – Hospital Management System
-- Dữ liệu khởi tạo đầy đủ cho development & testing TẤT CẢ MODULE
-- ============================================================================
-- Quy ước: @now = thời điểm chạy script, dùng để tạo dữ liệu "sống"
-- Lưu ý: INSERT IGNORE → chạy lại script sẽ không lỗi duplicate
-- ============================================================================

SET @now       = NOW(6);
SET @today     = CURDATE();
SET @yesterday = DATE_SUB(@today, INTERVAL 1 DAY);
SET @2days_ago = DATE_SUB(@today, INTERVAL 2 DAY);
SET @3days_ago = DATE_SUB(@today, INTERVAL 3 DAY);
SET @5days_ago = DATE_SUB(@today, INTERVAL 5 DAY);
SET @7days_ago = DATE_SUB(@today, INTERVAL 7 DAY);
SET @tomorrow  = DATE_ADD(@today, INTERVAL 1 DAY);
SET @2days_later = DATE_ADD(@today, INTERVAL 2 DAY);
SET @3days_later = DATE_ADD(@today, INTERVAL 3 DAY);

-- ============================================================================
-- 1. DEPARTMENTS (5 phòng ban / chuyên khoa)
-- ============================================================================
INSERT IGNORE INTO departments (id, dept_name, location, phone, created_at, updated_at) VALUES
(1, 'Noi khoa',       'Tang 2 - Khu A', '028-1001', @now, @now),
(2, 'Ngoai khoa',     'Tang 3 - Khu A', '028-1002', @now, @now),
(3, 'Nhi khoa',       'Tang 2 - Khu B', '028-1003', @now, @now),
(4, 'Tim mach',       'Tang 4 - Khu A', '028-1004', @now, @now),
(5, 'Xet nghiem',     'Tang 1 - Khu C', '028-1005', @now, @now);

-- ============================================================================
-- 2. DOCTORS (5 bác sĩ thuộc các chuyên khoa khác nhau)
-- ============================================================================
INSERT IGNORE INTO doctors (id, full_name, phone, email, license_number, hire_date, department_id, created_at, updated_at) VALUES
(1, 'BS. Nguyen Van An',    '0901000001', 'an.nguyen@hospital.local',    'LIC-IM-001',  '2020-03-01', 1, @now, @now),
(2, 'BS. Tran Thi Binh',    '0901000002', 'binh.tran@hospital.local',    'LIC-SUR-001', '2019-07-15', 2, @now, @now),
(3, 'BS. Le Minh Chau',     '0901000003', 'chau.le@hospital.local',      'LIC-CAR-001', '2021-01-10', 4, @now, @now),
(4, 'BS. Pham Hoang Duc',   '0901000004', 'duc.pham@hospital.local',     'LIC-PED-001', '2022-06-01', 3, @now, @now),
(5, 'BS. Vo Thanh Em',      '0901000005', 'em.vo@hospital.local',        'LIC-IM-002',  '2018-11-20', 1, @now, @now);

-- ============================================================================
-- 3. MEDICINES (20 loại thuốc – danh mục tĩnh, đủ nhóm)
-- ============================================================================
INSERT IGNORE INTO medicines (id, medicine_name, generic_name, category, unit, unit_price, insurance_covered, active, created_at, updated_at) VALUES
-- Kháng sinh
(1,  'Amoxicillin 500mg',    'Amoxicillin',       'Khang sinh', 'Vien',  2500.00,  b'1', b'1', @now, @now),
(2,  'Cefixime 200mg',       'Cefixime',          'Khang sinh', 'Vien',  6500.00,  b'1', b'1', @now, @now),
(3,  'Azithromycin 500mg',   'Azithromycin',      'Khang sinh', 'Vien',  12000.00, b'1', b'1', @now, @now),
-- Giảm đau
(4,  'Paracetamol 500mg',    'Acetaminophen',     'Giam dau',   'Vien',  1200.00,  b'1', b'1', @now, @now),
(5,  'Ibuprofen 400mg',      'Ibuprofen',         'Giam dau',   'Vien',  1800.00,  b'1', b'1', @now, @now),
(6,  'Diclofenac 50mg',      'Diclofenac',        'Giam dau',   'Vien',  2200.00,  b'0', b'1', @now, @now),
-- Vitamin
(7,  'Vitamin C 500mg',      'Ascorbic Acid',     'Vitamin',    'Vien',  1000.00,  b'0', b'1', @now, @now),
(8,  'Vitamin D3 1000IU',    'Cholecalciferol',   'Vitamin',    'Vien',  1500.00,  b'0', b'1', @now, @now),
(9,  'B Complex',            'Vitamin B Complex', 'Vitamin',    'Vien',  2000.00,  b'0', b'1', @now, @now),
-- Tiêu hóa
(10, 'Omeprazole 20mg',      'Omeprazole',        'Tieu hoa',   'Vien',  3000.00,  b'1', b'1', @now, @now),
(11, 'Esomeprazole 40mg',    'Esomeprazole',      'Tieu hoa',   'Vien',  5500.00,  b'1', b'1', @now, @now),
(12, 'Loperamide 2mg',       'Loperamide',        'Tieu hoa',   'Vien',  1700.00,  b'0', b'1', @now, @now),
-- Tim mạch
(13, 'Amlodipine 5mg',       'Amlodipine',        'Tim mach',   'Vien',  2500.00,  b'1', b'1', @now, @now),
(14, 'Losartan 50mg',        'Losartan',          'Tim mach',   'Vien',  3200.00,  b'1', b'1', @now, @now),
(15, 'Atorvastatin 20mg',    'Atorvastatin',      'Tim mach',   'Vien',  4200.00,  b'1', b'1', @now, @now),
-- Dị ứng
(16, 'Cetirizine 10mg',      'Cetirizine',        'Di ung',     'Vien',  1600.00,  b'0', b'1', @now, @now),
(17, 'Loratadine 10mg',      'Loratadine',        'Di ung',     'Vien',  1800.00,  b'0', b'1', @now, @now),
-- Hô hấp
(18, 'Salbutamol Inhaler',   'Salbutamol',        'Ho hap',     'Hop',   85000.00, b'1', b'1', @now, @now),
-- Nội tiết
(19, 'Metformin 500mg',      'Metformin',         'Noi tiet',   'Vien',  1400.00,  b'1', b'1', @now, @now),
(20, 'Insulin Regular',      'Insulin Human',     'Noi tiet',   'Lo',    125000.00,b'1', b'1', @now, @now);

-- ============================================================================
-- 4. ROLES (4 vai trò hệ thống)
-- ============================================================================
INSERT IGNORE INTO roles (id, role_name, created_at, updated_at) VALUES
(1, 'ADMIN',   @now, @now),
(2, 'DOCTOR',  @now, @now),
(3, 'NURSE',   @now, @now),
(4, 'CASHIER', @now, @now),
(5, 'PATIENT', @now, @now);

-- ============================================================================
-- 5. USERS (7 tài khoản – đủ 4 role, có inactive user để test)
-- ============================================================================
-- Mật khẩu BCrypt: admin/admin123, doctor1/doctor123, doctor2/doctor123,
--                  doctor3/doctor123, nurse1/nurse123, cashier1/cashier123,
--                  nurse_inactive/nurse123
INSERT IGNORE INTO users (id, username, password_hash, email, full_name, is_active, doctor_id, created_at, updated_at) VALUES
(1, 'admin',           '$2a$10$C4j99tjEM/GY82xwqqi9cuNE0U4PPWSxrW8dlaiN5P1qQXOQUsZ4i', 'admin@hospital.local',           'System Administrator',     b'1', NULL, @now, @now),
(2, 'doctor1',         '$2a$10$v2f/o/5p8aQouFu.jsZSGOwhf4GwwoveUd6/wJ0XYftgc.dBpmL2K', 'doctor1@hospital.local',         'BS. Nguyen Van An',        b'1', 1,    @now, @now),
(3, 'doctor2',         '$2a$10$v2f/o/5p8aQouFu.jsZSGOwhf4GwwoveUd6/wJ0XYftgc.dBpmL2K', 'doctor2@hospital.local',         'BS. Tran Thi Binh',        b'1', 2,    @now, @now),
(4, 'nurse1',          '$2a$10$QgJMU9tVoguWGmWFf2.iMOPT8Thw2Wc5NkVp/f5eQUtXslVj76KpK', 'nurse1@hospital.local',          'Y ta Le Thi Hoa',          b'1', NULL, @now, @now),
(5, 'cashier1',        '$2a$10$9S6QNL4JaUUHXEs0jqq9Ku19u5U/62SLT8G/pFTHI1vAMPfoMjqJ.', 'cashier1@hospital.local',        'Thu ngan Pham Van Tai',    b'1', NULL, @now, @now),
(6, 'doctor3',         '$2a$10$v2f/o/5p8aQouFu.jsZSGOwhf4GwwoveUd6/wJ0XYftgc.dBpmL2K', 'doctor3@hospital.local',         'BS. Le Minh Chau',         b'1', 3,    @now, @now),
(7, 'nurse_inactive',  '$2a$10$QgJMU9tVoguWGmWFf2.iMOPT8Thw2Wc5NkVp/f5eQUtXslVj76KpK', 'nurse_inactive@hospital.local',  'Y ta Nguyen Van Nghi viec', b'0', NULL, @now, @now);

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(1, 1),   -- admin   → ADMIN
(2, 2),   -- doctor1 → DOCTOR
(3, 2),   -- doctor2 → DOCTOR
(4, 3),   -- nurse1  → NURSE
(5, 4),   -- cashier1 → CASHIER
(6, 2),   -- doctor3 → DOCTOR
(7, 3);   -- nurse_inactive → NURSE (bị vô hiệu hóa)

-- ============================================================================
-- 6. PATIENTS (10 bệnh nhân – đa dạng profiles)
-- ============================================================================
INSERT IGNORE INTO patients (id, full_name, dob, gender, cccd, phone, address, blood_type, insurance_number, created_at, updated_at) VALUES
-- Bệnh nhân CÓ bảo hiểm (test BHYT 80%)
(1,  'Nguyen Van Hung',     '1990-01-15', 'MALE',   '012345678901', '0909000001', '123 Le Loi, Q1, HCMC',              'A+',  'DN4567890123',  @now, @now),
(2,  'Tran Thi Mai',        '1985-05-20', 'FEMALE', '012345678902', '0909000002', '456 Nguyen Hue, Q1, HCMC',          'B+',  'DN1234567890',  @now, @now),
(3,  'Le Hoang Nam',        '1978-11-03', 'MALE',   '012345678903', '0909000003', '789 Hai Ba Trung, Q3, HCMC',        'O+',  'HN9876543210',  @now, @now),
(4,  'Pham Thi Lan',        '2000-03-08', 'FEMALE', '012345678904', '0909000004', '321 Vo Van Tan, Q3, HCMC',          'AB+', 'SG5555666677',  @now, @now),
-- Bệnh nhân KHÔNG có bảo hiểm (test tự trả 100%)
(5,  'Vo Minh Tuan',        '1995-07-22', 'MALE',   '012345678905', '0909000005', '654 Cach Mang Thang 8, Q10, HCMC',  'A-',  NULL,            @now, @now),
(6,  'Hoang Thi Thao',      '1988-12-30', 'FEMALE', '012345678906', '0909000006', '987 Tran Hung Dao, Q5, HCMC',       'O-',  NULL,            @now, @now),
(7,  'Do Quang Minh',       '1975-09-14', 'MALE',   '012345678907', '0909000007', '147 Ly Tu Trong, Q1, HCMC',         'B-',  NULL,            @now, @now),
-- Bệnh nhân trẻ em (test Nhi khoa)
(8,  'Nguyen Bao An',       '2020-06-01', 'MALE',   '012345678908', '0909000008', '258 Le Van Sy, Q3, HCMC',           'A+',  'TE1234567890',  @now, @now),
-- Bệnh nhân cao tuổi (test Tim mạch)
(9,  'Tran Van Duc',        '1950-02-28', 'MALE',   '012345678909', '0909000009', '369 Nguyen Thi Minh Khai, Q1, HCMC','AB-', 'NCT987654321',  @now, @now),
-- Bệnh nhân mới (chưa có lịch hẹn nào – test tạo mới)
(10, 'Le Thi Ngoc Anh',     '1998-04-17', 'FEMALE', '012345678910', '0909000010', '741 Pham Van Dong, Thu Duc, HCMC',  'O+',  NULL,            @now, @now);

-- ============================================================================
-- 7. APPOINTMENTS (15 lịch hẹn – cover TẤT CẢ trạng thái)
-- ============================================================================
INSERT IGNORE INTO appointments (id, patient_id, doctor_id, appt_datetime, status, notes, created_at, updated_at) VALUES
-- === ĐÃ HOÀN THÀNH (COMPLETED) – có medical record, đơn thuốc, xét nghiệm, hóa đơn ===
(1,  1, 1, TIMESTAMP(@7days_ago, '08:00:00'), 'COMPLETED',  'Kham tong quat, benh nhan ho nhieu',       TIMESTAMP(@7days_ago, '07:30:00'), @now),
(2,  2, 2, TIMESTAMP(@5days_ago, '09:00:00'), 'COMPLETED',  'Kham ngoai khoa, dau bung',                TIMESTAMP(@5days_ago, '08:30:00'), @now),
(3,  3, 3, TIMESTAMP(@3days_ago, '10:00:00'), 'COMPLETED',  'Kham tim mach dinh ky',                    TIMESTAMP(@3days_ago, '09:30:00'), @now),
(4,  5, 1, TIMESTAMP(@2days_ago, '14:00:00'), 'COMPLETED',  'Kham noi khoa, cam cum thong thuong',       TIMESTAMP(@2days_ago, '13:30:00'), @now),
(5,  9, 3, TIMESTAMP(@yesterday, '08:30:00'), 'COMPLETED',  'Kham tim mach, benh nhan cao tuoi',         TIMESTAMP(@yesterday, '08:00:00'), @now),
(6,  4, 4, TIMESTAMP(@yesterday, '10:00:00'), 'COMPLETED',  'Kham nhi khoa cho be',                      TIMESTAMP(@yesterday, '09:30:00'), @now),

-- === ĐÃ CHECK-IN (CHECKED_IN) – đang khám, chưa xong ===
(7,  6, 1, TIMESTAMP(@today, '08:00:00'),     'CHECKED_IN', 'Kham dau dau, chong mat',                  TIMESTAMP(@yesterday, '16:00:00'), @now),
(8,  7, 2, TIMESTAMP(@today, '09:00:00'),     'CHECKED_IN', 'Kham viem ruot thua nghi ngo',              TIMESTAMP(@yesterday, '17:00:00'), @now),

-- === ĐÃ XÁC NHẬN (CONFIRMED) – chờ check-in, dùng để test tiếp nhận ===
(9,  8, 4, TIMESTAMP(@today, '10:30:00'),     'CONFIRMED',  'Kham suc khoe be',                         TIMESTAMP(@yesterday, '18:00:00'), @now),
(10, 1, 5, TIMESTAMP(@today, '14:00:00'),     'CONFIRMED',  'Tai kham sau 1 tuan',                      TIMESTAMP(@2days_ago, '10:00:00'), @now),
(11, 2, 1, TIMESTAMP(@today, '15:00:00'),     'CONFIRMED',  'Kham lai ket qua xet nghiem',              TIMESTAMP(@2days_ago, '11:00:00'), @now),

-- === CHỜ XÁC NHẬN (PENDING) – mới đặt ===
(12, 10, 1, TIMESTAMP(@tomorrow, '08:00:00'),  'PENDING',   'Kham lan dau, dau bung am i',              @now, @now),
(13, 4,  3, TIMESTAMP(@tomorrow, '10:00:00'),  'PENDING',   'Kham tim mach theo yeu cau',               @now, @now),
(14, 6,  5, TIMESTAMP(@2days_later, '09:00:00'), 'PENDING', 'Kham tong quat hang nam',                  @now, @now),

-- === ĐÃ HỦY (CANCELLED) – test hủy lịch ===
(15, 7,  4, TIMESTAMP(@yesterday, '14:00:00'), 'CANCELLED', 'Benh nhan xin huy vi ban dot xuat',        TIMESTAMP(@3days_ago, '10:00:00'), @now);

-- ============================================================================
-- 8. MEDICAL RECORDS (10 hồ sơ khám – cho các appointment COMPLETED & CHECKED_IN)
-- ============================================================================
INSERT IGNORE INTO medical_records (id, patient_id, doctor_id, appointment_id, diagnosis, visit_date, notes, created_at, updated_at) VALUES
-- Từ appointment COMPLETED
(1,  1, 1, 1,  'Viem hong hat cap',                     @7days_ago, 'Benh nhan ho khan, viem hong do, co BHYT. Can theo doi them 1 tuan.',                @now, @now),
(2,  2, 2, 2,  'Viem ruot thua man tinh',               @5days_ago, 'Dau bung vung chau phai, xet nghiem mau binh thuong. Hen tai kham.',                  @now, @now),
(3,  3, 3, 3,  'Tang huyet ap do 2, roi loan lipid mau', @3days_ago, 'Huyet ap 160/95, cholesterol cao. Chi dinh thuoc va che do an.',                      @now, @now),
(4,  5, 1, 4,  'Cam cum thong thuong',                  @2days_ago, 'Sot 38.5 do, chay nuoc mui, khong co BHYT.',                                          @now, @now),
(5,  9, 3, 5,  'Suy tim do 2 NYHA',                     @yesterday, 'Benh nhan 76 tuoi, kho tho khi gang suc, phu chan nhe. Co BHYT.',                      @now, @now),
(6,  4, 4, 6,  'Viem phe quan cap o tre',               @yesterday, 'Be 6 tuoi, ho co dom, sot nhe 37.8. Co BHYT tre em.',                                  @now, @now),

-- Từ appointment CHECKED_IN (đang khám, chưa có chẩn đoán đầy đủ)
(7,  6, 1, 7,  NULL,                                    @today,     'Dang tham kham, benh nhan than dau dau va chong mat tu 3 ngay truoc.',                  @now, @now),
(8,  7, 2, 8,  NULL,                                    @today,     'Dang tham kham, nghi ngo viem ruot thua. Can sieu am bung.',                             @now, @now),

-- Hồ sơ cũ KHÔNG gắn appointment (walk-in simulation từ tháng trước)
(9,  1, 5, NULL, 'Viem da day man tinh',               DATE_SUB(@today, INTERVAL 30 DAY), 'Kham truc tiep khong hen truoc. Da kham va ke don.',             @now, @now),
(10, 3, 1, NULL, 'Cam cum theo mua',                   DATE_SUB(@today, INTERVAL 14 DAY), 'Kham walk-in, sot nhe, ho, so mui.',                              @now, @now);

-- ============================================================================
-- 9. PRESCRIPTIONS (8 đơn thuốc – cover DRAFT, COMPLETED)
-- ============================================================================
INSERT IGNORE INTO prescriptions (id, medical_record_id, doctor_id, issued_date, status, created_at, updated_at) VALUES
-- Đơn thuốc đã phát (COMPLETED)
(1, 1, 1, @7days_ago, 'COMPLETED', @now, @now),   -- Viêm họng → Kháng sinh + giảm đau
(2, 2, 2, @5days_ago, 'COMPLETED', @now, @now),   -- Viêm ruột → Kháng sinh + tiêu hóa
(3, 3, 3, @3days_ago, 'COMPLETED', @now, @now),   -- Tim mạch → thuốc huyết áp + mỡ máu
(4, 4, 1, @2days_ago, 'COMPLETED', @now, @now),   -- Cảm cúm → giảm đau + vitamin
(5, 5, 3, @yesterday, 'COMPLETED', @now, @now),   -- Suy tim → thuốc tim mạch
(6, 6, 4, @yesterday, 'COMPLETED', @now, @now),   -- Viêm phế quản trẻ → kháng sinh + ho

-- Đơn nháp (DRAFT) – bác sĩ đang kê, chưa hoàn tất
(7, 9, 5, DATE_SUB(@today, INTERVAL 30 DAY), 'COMPLETED', @now, @now),  -- Viêm dạ dày cũ
(8, 10, 1, DATE_SUB(@today, INTERVAL 14 DAY), 'COMPLETED', @now, @now); -- Cảm cúm walk-in

-- ============================================================================
-- 10. PRESCRIPTION ITEMS (chi tiết thuốc trong mỗi đơn)
-- ============================================================================
INSERT IGNORE INTO prescription_items (id, prescription_id, medicine_id, quantity, dosage, instructions, unit_price_at_time, created_at, updated_at) VALUES
-- Đơn 1: Viêm họng (Amoxicillin + Paracetamol + Vitamin C)
(1,  1, 1,  20, '2 vien/ngay',  'Sang 1 vien, chieu 1 vien, uong sau an',           2500.00,  @now, @now),
(2,  1, 4,  10, '1 vien/lan',   'Uong khi sot tren 38.5 do, cach 4-6 tieng',        1200.00,  @now, @now),
(3,  1, 7,  14, '1 vien/ngay',  'Uong sau an sang',                                  1000.00,  @now, @now),

-- Đơn 2: Viêm ruột (Cefixime + Omeprazole + Loperamide)
(4,  2, 2,  14, '2 vien/ngay',  'Sang 1, toi 1, uong truoc an 30 phut',              6500.00,  @now, @now),
(5,  2, 10, 14, '1 vien/ngay',  'Uong truoc an sang 30 phut',                        3000.00,  @now, @now),
(6,  2, 12,  6, '1 vien/lan',   'Uong khi tieu chay, toi da 4 vien/ngay',            1700.00,  @now, @now),

-- Đơn 3: Tim mạch (Amlodipine + Losartan + Atorvastatin)
(7,  3, 13, 30, '1 vien/ngay',  'Uong sang, theo doi huyet ap hang ngay',            2500.00,  @now, @now),
(8,  3, 14, 30, '1 vien/ngay',  'Uong buoi toi truoc khi ngu',                       3200.00,  @now, @now),
(9,  3, 15, 30, '1 vien/ngay',  'Uong buoi toi, kiem tra lipid mau sau 3 thang',     4200.00,  @now, @now),

-- Đơn 4: Cảm cúm (Paracetamol + Cetirizine + Vitamin C)
(10, 4, 4,  10, '1 vien/lan',   'Uong khi sot, cach 4-6 tieng',                      1200.00,  @now, @now),
(11, 4, 16,  7, '1 vien/ngay',  'Uong buoi toi truoc khi ngu',                       1600.00,  @now, @now),
(12, 4, 7,   7, '1 vien/ngay',  'Uong sang sau an',                                  1000.00,  @now, @now),

-- Đơn 5: Suy tim (Amlodipine + Losartan + Metformin)
(13, 5, 13, 30, '1 vien/ngay',  'Uong sang, khong ngung thuoc dot ngot',              2500.00,  @now, @now),
(14, 5, 14, 30, '1 vien/ngay',  'Uong toi',                                          3200.00,  @now, @now),
(15, 5, 19, 60, '2 vien/ngay',  'Sang 1, toi 1, uong ngay sau an',                   1400.00,  @now, @now),

-- Đơn 6: Viêm phế quản trẻ (Amoxicillin liều trẻ em + Paracetamol)
(16, 6, 1,  14, '1 vien/ngay',  'Lieu tre em: 1 vien/ngay, pha vao nuoc',            2500.00,  @now, @now),
(17, 6, 4,   7, '1/2 vien/lan', 'Khi sot, toi da 3 lan/ngay',                        1200.00,  @now, @now),

-- Đơn 7: Viêm dạ dày cũ (Esomeprazole + B Complex)
(18, 7, 11, 28, '1 vien/ngay',  'Uong truoc an sang 30 phut',                        5500.00,  @now, @now),
(19, 7, 9,  28, '1 vien/ngay',  'Uong sau an trua',                                  2000.00,  @now, @now),

-- Đơn 8: Cảm cúm walk-in (Paracetamol + Loratadine)
(20, 8, 4,  10, '1 vien/lan',   'Uong khi sot',                                      1200.00,  @now, @now),
(21, 8, 17,  5, '1 vien/ngay',  'Uong toi truoc khi ngu',                            1800.00,  @now, @now);

-- ============================================================================
-- 11. LAB TESTS (12 xét nghiệm – cover TẤT CẢ trạng thái)
-- ============================================================================
INSERT IGNORE INTO lab_tests (id, medical_record_id, test_type, ordered_by, result, result_file_url, status, test_date, fee, created_at, updated_at) VALUES
-- MR1: Viêm họng → xét nghiệm máu + CRP (COMPLETED)
(1,  1, 'Xet nghiem cong thuc mau',       1, 'Bach cau: 11.2 (tang nhe), Tieu cau: 250, Hb: 14.5 g/dL. Ket luan: Phan ung viem nhe.', NULL, 'COMPLETED', TIMESTAMP(@7days_ago, '09:30:00'), 120000.00, @now, @now),
(2,  1, 'Xet nghiem CRP (C-Reactive Protein)', 1, 'CRP: 18 mg/L (tang, binh thuong < 5). Xac nhan tinh trang viem cap.', NULL, 'COMPLETED', TIMESTAMP(@7days_ago, '09:45:00'), 85000.00, @now, @now),

-- MR2: Viêm ruột → siêu âm + xét nghiệm máu (COMPLETED)
(3,  2, 'Sieu am bung tong quat',          2, 'Ruot thua viem man, khong co dich tu do. Khong can phau thuat.', NULL, 'COMPLETED', TIMESTAMP(@5days_ago, '10:30:00'), 200000.00, @now, @now),
(4,  2, 'Xet nghiem mau sinh hoa',         2, 'GOT: 25, GPT: 30, Creatinine: 0.9, Glucose: 5.2 mmol/L. Tat ca binh thuong.', NULL, 'COMPLETED', TIMESTAMP(@5days_ago, '10:00:00'), 150000.00, @now, @now),

-- MR3: Tim mạch → xét nghiệm mỡ máu + ECG (COMPLETED)
(5,  3, 'Xet nghiem lipid mau',            3, 'Cholesterol: 6.8 (cao), LDL: 4.5 (cao), HDL: 1.0 (thap), Triglyceride: 2.8 (cao). Can dung thuoc.', NULL, 'COMPLETED', TIMESTAMP(@3days_ago, '11:00:00'), 180000.00, @now, @now),
(6,  3, 'Dien tam do (ECG)',               3, 'Nhip xoang deu, tan so 78 lan/phut. Khong bat thuong dang luu y.', NULL, 'COMPLETED', TIMESTAMP(@3days_ago, '11:30:00'), 100000.00, @now, @now),

-- MR5: Suy tim → xét nghiệm BNP + X-quang ngực (COMPLETED)
(7,  5, 'Xet nghiem BNP (NT-proBNP)',      3, 'NT-proBNP: 850 pg/mL (tang, binh thuong < 125). Goi y suy tim.', NULL, 'COMPLETED', TIMESTAMP(@yesterday, '09:30:00'), 250000.00, @now, @now),
(8,  5, 'X-quang nguc thang',              3, 'Bong tim to, chi so tim nguc 0.58. Sung huyet phe vi nhe.', NULL, 'COMPLETED', TIMESTAMP(@yesterday, '10:00:00'), 150000.00, @now, @now),

-- MR7: Đang khám (CHECKED_IN) → xét nghiệm ĐANG CHỜ (ORDERED) – test pending queue
(9,  7, 'CT Scanner so nao',               1, NULL, NULL, 'ORDERED', NULL, 500000.00, @now, @now),
(10, 7, 'Xet nghiem mau toan phan',        1, NULL, NULL, 'ORDERED', NULL, 120000.00, @now, @now),

-- MR8: Đang khám → xét nghiệm ĐÃ LẤY MẪU (SAMPLE_COLLECTED) – test trạng thái trung gian
(11, 8, 'Sieu am bung cap cuu',            2, NULL, NULL, 'SAMPLE_COLLECTED', TIMESTAMP(@today, '09:30:00'), 250000.00, @now, @now),

-- MR6: Viêm phế quản trẻ → xét nghiệm (COMPLETED)
(12, 6, 'Xet nghiem mau',                  4, 'Bach cau: 13.5 (tang nhe do nhiem trung). Tieu cau binh thuong.', NULL, 'COMPLETED', TIMESTAMP(@yesterday, '11:00:00'), 120000.00, @now, @now);

-- ============================================================================
-- 12. INVOICES (10 hóa đơn – cover PAID, PENDING, CANCELLED + đa dạng payment)
-- ============================================================================
INSERT IGNORE INTO invoices (id, medical_record_id, patient_id, examination_fee, medicine_fee, lab_fee, total_amount, insurance_coverage, insurance_amount, paid_amount, payment_method, status, paid_at, notes, created_at, updated_at) VALUES
-- === HÓA ĐƠN ĐÃ THANH TOÁN (PAID) – 7 ngày gần đây để test báo cáo ===

-- Hóa đơn 1: MR1 – Viêm họng, có BHYT 80%, trả tiền mặt
-- medicine: (20*2500 + 10*1200 + 14*1000) = 76000, lab: (120000 + 85000) = 205000
-- total: 150000 + 76000 + 205000 = 431000, BHYT 80% = 344800, patient pays = 86200
(1,  1, 1, 150000.00, 76000.00,  205000.00, 431000.00,  'EIGHTY', 344800.00, 86200.00,  'CASH',     'PAID', TIMESTAMP(@7days_ago, '11:00:00'), NULL, TIMESTAMP(@7days_ago, '08:30:00'), @now),

-- Hóa đơn 2: MR2 – Viêm ruột, có BHYT 80%, chuyển khoản
-- medicine: (14*6500 + 14*3000 + 6*1700) = 143200, lab: (200000 + 150000) = 350000
-- total: 150000 + 143200 + 350000 = 643200, BHYT 80% = 514560, patient = 128640
(2,  2, 2, 150000.00, 143200.00, 350000.00, 643200.00,  'EIGHTY', 514560.00, 128640.00, 'TRANSFER', 'PAID', TIMESTAMP(@5days_ago, '12:00:00'), NULL, TIMESTAMP(@5days_ago, '10:30:00'), @now),

-- Hóa đơn 3: MR3 – Tim mạch, có BHYT 80%, tiền mặt
-- medicine: (30*2500 + 30*3200 + 30*4200) = 297000, lab: (180000 + 100000) = 280000
-- total: 150000 + 297000 + 280000 = 727000, BHYT 80% = 581600, patient = 145400
(3,  3, 3, 150000.00, 297000.00, 280000.00, 727000.00,  'EIGHTY', 581600.00, 145400.00, 'CASH',     'PAID', TIMESTAMP(@3days_ago, '13:00:00'), NULL, TIMESTAMP(@3days_ago, '10:30:00'), @now),

-- Hóa đơn 4: MR4 – Cảm cúm, KHÔNG có BHYT, chuyển khoản
-- medicine: (10*1200 + 7*1600 + 7*1000) = 30200, lab: 0
-- total: 150000 + 30200 + 0 = 180200, BHYT 0%, patient pays all
(4,  4, 5, 150000.00, 30200.00,  0,         180200.00,  'NONE',   0,         180200.00, 'TRANSFER', 'PAID', TIMESTAMP(@2days_ago, '15:00:00'), NULL, TIMESTAMP(@2days_ago, '14:00:00'), @now),

-- Hóa đơn 5: MR5 – Suy tim, có BHYT 80%, tiền mặt
-- medicine: (30*2500 + 30*3200 + 60*1400) = 255000, lab: (250000 + 150000) = 400000
-- total: 150000 + 255000 + 400000 = 805000, BHYT 80% = 644000, patient = 161000
(5,  5, 9, 150000.00, 255000.00, 400000.00, 805000.00,  'EIGHTY', 644000.00, 161000.00, 'CASH',     'PAID', TIMESTAMP(@yesterday, '11:30:00'), NULL, TIMESTAMP(@yesterday, '09:00:00'), @now),

-- Hóa đơn 6: MR6 – Nhi khoa, có BHYT trẻ em 80%, tiền mặt
-- medicine: (14*2500 + 7*1200) = 43400, lab: 120000
-- total: 150000 + 43400 + 120000 = 313400, BHYT 80% = 250720, patient = 62680
(6,  6, 4, 150000.00, 43400.00,  120000.00, 313400.00,  'EIGHTY', 250720.00, 62680.00,  'CASH',     'PAID', TIMESTAMP(@yesterday, '13:00:00'), NULL, TIMESTAMP(@yesterday, '10:30:00'), @now),

-- Hóa đơn MR9: Viêm dạ dày cũ (walk-in, 30 ngày trước), có BHYT, tiền mặt
-- medicine: (28*5500 + 28*2000) = 210000, lab: 0
-- total: 150000 + 210000 + 0 = 360000, BHYT 80% = 288000, patient = 72000
(7,  9, 1, 150000.00, 210000.00, 0,         360000.00,  'EIGHTY', 288000.00, 72000.00,  'CASH',     'PAID', TIMESTAMP(DATE_SUB(@today, INTERVAL 30 DAY), '14:00:00'), NULL, TIMESTAMP(DATE_SUB(@today, INTERVAL 30 DAY), '11:00:00'), @now),

-- Hóa đơn MR10: Cảm cúm walk-in (14 ngày trước), KHÔNG BHYT
-- medicine: (10*1200 + 5*1800) = 21000, lab: 0
-- total: 150000 + 21000 + 0 = 171000, NONE, patient = 171000
(8,  10, 3, 150000.00, 21000.00, 0,         171000.00,  'NONE',   0,         171000.00, 'TRANSFER', 'PAID', TIMESTAMP(DATE_SUB(@today, INTERVAL 14 DAY), '15:00:00'), NULL, TIMESTAMP(DATE_SUB(@today, INTERVAL 14 DAY), '12:00:00'), @now),

-- === HÓA ĐƠN CHỜ THANH TOÁN (PENDING) – test cho cashier ===
-- MR7: Đang khám, hóa đơn tạm (chưa đầy đủ, phí xét nghiệm chưa tính)
(9,  7, 6, 150000.00, 0,         620000.00, 770000.00,  'NONE',   0,         770000.00, NULL,       'PENDING', NULL, 'Benh nhan dang kham, hoa don tam tinh', @now, @now),

-- === HÓA ĐƠN ĐÃ HỦY (CANCELLED) – test trạng thái hủy ===
(10, 8, 7, 150000.00, 0,         250000.00, 400000.00,  'NONE',   0,         400000.00, NULL,       'CANCELLED', NULL, 'Hoa don bi huy do benh nhan chuyen vien', @now, @now);

-- ============================================================================
-- 13. AUDIT LOGS (mẫu – các hành động quan trọng)
-- ============================================================================
INSERT IGNORE INTO audit_logs (id, user_id, action, entity_type, entity_id, old_value, new_value, ip_address, created_at) VALUES
(1,  1, 'CREATE',          'Patient',       1,  NULL, '{"fullName":"Nguyen Van Hung","cccd":"012345678901"}',                      '127.0.0.1',   TIMESTAMP(@7days_ago, '07:00:00')),
(2,  1, 'CREATE',          'Patient',       2,  NULL, '{"fullName":"Tran Thi Mai","cccd":"012345678902"}',                          '127.0.0.1',   TIMESTAMP(@7days_ago, '07:05:00')),
(3,  4, 'CREATE',          'Appointment',   1,  NULL, '{"patientId":1,"doctorId":1,"status":"PENDING"}',                            '192.168.1.10', TIMESTAMP(@7days_ago, '07:30:00')),
(4,  4, 'UPDATE_STATUS',   'Appointment',   1,  '{"status":"PENDING"}', '{"status":"CONFIRMED"}',                                  '192.168.1.10', TIMESTAMP(@7days_ago, '07:35:00')),
(5,  4, 'CHECK_IN',        'MedicalRecord', 1,  NULL, '{"appointmentId":1,"patientId":1}',                                         '192.168.1.10', TIMESTAMP(@7days_ago, '08:00:00')),
(6,  2, 'CREATE',          'Prescription',  1,  NULL, '{"medicalRecordId":1,"items":3}',                                            '192.168.1.20', TIMESTAMP(@7days_ago, '09:00:00')),
(7,  2, 'CREATE_LAB_TEST', 'LabTest',       1,  NULL, '{"testType":"Xet nghiem cong thuc mau","medicalRecordId":1}',                '192.168.1.20', TIMESTAMP(@7days_ago, '09:15:00')),
(8,  4, 'UPDATE_LAB_TEST_RESULT', 'LabTest', 1, '{"status":"ORDERED"}', '{"status":"COMPLETED","result":"Bach cau tang nhe"}',      '192.168.1.10', TIMESTAMP(@7days_ago, '09:30:00')),
(9,  1, 'CREATE',          'Invoice',       1,  NULL, '{"medicalRecordId":1,"totalAmount":431000}',                                 '127.0.0.1',   TIMESTAMP(@7days_ago, '10:00:00')),
(10, 5, 'UPDATE',          'Invoice',       1,  '{"status":"PENDING"}', '{"status":"PAID","paymentMethod":"CASH"}',                 '192.168.1.30', TIMESTAMP(@7days_ago, '11:00:00')),
(11, 1, 'CREATE',          'User',          7,  NULL, '{"username":"nurse_inactive","roles":["NURSE"]}',                            '127.0.0.1',   TIMESTAMP(@5days_ago, '08:00:00')),
(12, 1, 'TOGGLE_ACTIVE',   'User',          7,  '{"isActive":true}', '{"isActive":false}',                                         '127.0.0.1',   TIMESTAMP(@3days_ago, '09:00:00')),
(13, 1, 'CREATE',          'Patient',       10, NULL, '{"fullName":"Le Thi Ngoc Anh","cccd":"012345678910"}',                       '127.0.0.1',   TIMESTAMP(@2days_ago, '08:00:00')),
(14, 6, 'CREATE',          'Appointment',   13, NULL, '{"patientId":4,"doctorId":3,"status":"PENDING"}',                            '192.168.1.40', @now),
(15, 1, 'RESET_PASSWORD',  'User',          4,  NULL, '{"userId":4,"action":"password_reset"}',                                     '127.0.0.1',   TIMESTAMP(@yesterday, '16:00:00'));
