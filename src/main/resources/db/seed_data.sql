-- Seed data for local development and team onboarding.
SET @now = NOW(6);

INSERT IGNORE INTO departments (id, dept_name, location, phone, created_at, updated_at) VALUES
(1, 'Noi khoa', 'Tang 2 - Khu A', '028-1001', @now, @now),
(2, 'Ngoai khoa', 'Tang 3 - Khu A', '028-1002', @now, @now),
(3, 'Nhi khoa', 'Tang 2 - Khu B', '028-1003', @now, @now),
(4, 'Tim mach', 'Tang 4 - Khu A', '028-1004', @now, @now),
(5, 'Xet nghiem', 'Tang 1 - Khu C', '028-1005', @now, @now);

INSERT IGNORE INTO doctors (id, full_name, phone, email, license_number, hire_date, department_id, created_at, updated_at) VALUES
(1, 'Nguyen Van An', '0901000001', 'an.nguyen@hospital.local', 'LIC-IM-001', '2020-03-01', 1, @now, @now),
(2, 'Tran Thi Binh', '0901000002', 'binh.tran@hospital.local', 'LIC-SUR-001', '2019-07-15', 2, @now, @now),
(3, 'Le Minh Chau', '0901000003', 'chau.le@hospital.local', 'LIC-CAR-001', '2021-01-10', 4, @now, @now);

INSERT IGNORE INTO medicines (id, medicine_name, generic_name, category, unit, unit_price, created_at, updated_at) VALUES
(1, 'Amoxicillin 500mg', 'Amoxicillin', 'Khang sinh', 'Vien', 2500.00, @now, @now),
(2, 'Cefixime 200mg', 'Cefixime', 'Khang sinh', 'Vien', 6500.00, @now, @now),
(3, 'Azithromycin 500mg', 'Azithromycin', 'Khang sinh', 'Vien', 12000.00, @now, @now),
(4, 'Paracetamol 500mg', 'Acetaminophen', 'Giam dau', 'Vien', 1200.00, @now, @now),
(5, 'Ibuprofen 400mg', 'Ibuprofen', 'Giam dau', 'Vien', 1800.00, @now, @now),
(6, 'Diclofenac 50mg', 'Diclofenac', 'Giam dau', 'Vien', 2200.00, @now, @now),
(7, 'Vitamin C 500mg', 'Ascorbic Acid', 'Vitamin', 'Vien', 1000.00, @now, @now),
(8, 'Vitamin D3 1000IU', 'Cholecalciferol', 'Vitamin', 'Vien', 1500.00, @now, @now),
(9, 'B Complex', 'Vitamin B Complex', 'Vitamin', 'Vien', 2000.00, @now, @now),
(10, 'Omeprazole 20mg', 'Omeprazole', 'Tieu hoa', 'Vien', 3000.00, @now, @now),
(11, 'Esomeprazole 40mg', 'Esomeprazole', 'Tieu hoa', 'Vien', 5500.00, @now, @now),
(12, 'Loperamide 2mg', 'Loperamide', 'Tieu hoa', 'Vien', 1700.00, @now, @now),
(13, 'Amlodipine 5mg', 'Amlodipine', 'Tim mach', 'Vien', 2500.00, @now, @now),
(14, 'Losartan 50mg', 'Losartan', 'Tim mach', 'Vien', 3200.00, @now, @now),
(15, 'Atorvastatin 20mg', 'Atorvastatin', 'Tim mach', 'Vien', 4200.00, @now, @now),
(16, 'Cetirizine 10mg', 'Cetirizine', 'Di ung', 'Vien', 1600.00, @now, @now),
(17, 'Loratadine 10mg', 'Loratadine', 'Di ung', 'Vien', 1800.00, @now, @now),
(18, 'Salbutamol Inhaler', 'Salbutamol', 'Ho hap', 'Hop', 85000.00, @now, @now),
(19, 'Metformin 500mg', 'Metformin', 'Noi tiet', 'Vien', 1400.00, @now, @now),
(20, 'Insulin Regular', 'Insulin Human', 'Noi tiet', 'Lo', 125000.00, @now, @now);

INSERT IGNORE INTO roles (id, role_name, created_at, updated_at) VALUES
(1, 'ADMIN', @now, @now),
(2, 'DOCTOR', @now, @now),
(3, 'NURSE', @now, @now),
(4, 'CASHIER', @now, @now);

-- Training credentials: admin/admin123, doctor1/doctor123, doctor2/doctor123, nurse1/nurse123, cashier1/cashier123.
INSERT IGNORE INTO users (id, username, password_hash, email, is_active, doctor_id, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$C4j99tjEM/GY82xwqqi9cuNE0U4PPWSxrW8dlaiN5P1qQXOQUsZ4i', 'admin@hospital.local', b'1', NULL, @now, @now),
(2, 'doctor1', '$2a$10$v2f/o/5p8aQouFu.jsZSGOwhf4GwwoveUd6/wJ0XYftgc.dBpmL2K', 'doctor1@hospital.local', b'1', 1, @now, @now),
(3, 'doctor2', '$2a$10$v2f/o/5p8aQouFu.jsZSGOwhf4GwwoveUd6/wJ0XYftgc.dBpmL2K', 'doctor2@hospital.local', b'1', 2, @now, @now),
(4, 'nurse1', '$2a$10$QgJMU9tVoguWGmWFf2.iMOPT8Thw2Wc5NkVp/f5eQUtXslVj76KpK', 'nurse1@hospital.local', b'1', NULL, @now, @now),
(5, 'cashier1', '$2a$10$9S6QNL4JaUUHXEs0jqq9Ku19u5U/62SLT8G/pFTHI1vAMPfoMjqJ.', 'cashier1@hospital.local', b'1', NULL, @now, @now);

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(4, 3),
(5, 4);
