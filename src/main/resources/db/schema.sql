-- Database schema for the Hospital Management System base project.
CREATE TABLE IF NOT EXISTS patients
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    full_name
    VARCHAR
(
    150
) NOT NULL,
    dob DATE,
    gender VARCHAR
(
    20
),
    cccd VARCHAR
(
    20
),
    phone VARCHAR
(
    20
),
    address VARCHAR
(
    255
),
    blood_type VARCHAR
(
    10
),
    insurance_number VARCHAR
(
    50
),
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT uk_patients_cccd UNIQUE
(
    cccd
),
    INDEX idx_patients_cccd
(
    cccd
),
    INDEX idx_patients_phone
(
    phone
),
    INDEX idx_patients_full_name
(
    full_name
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS departments
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    dept_name
    VARCHAR
(
    120
) NOT NULL,
    location VARCHAR
(
    120
),
    phone VARCHAR
(
    20
),
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doctors
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    full_name
    VARCHAR
(
    150
) NOT NULL,
    phone VARCHAR
(
    20
),
    email VARCHAR
(
    120
),
    license_number VARCHAR
(
    50
),
    hire_date DATE,
    department_id BIGINT,
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT uk_doctors_license_number UNIQUE
(
    license_number
),
    CONSTRAINT fk_doctors_department FOREIGN KEY
(
    department_id
) REFERENCES departments
(
    id
),
    INDEX idx_doctors_license_number
(
    license_number
),
    INDEX idx_doctors_department_id
(
    department_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS appointments
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    patient_id
    BIGINT
    NOT
    NULL,
    doctor_id
    BIGINT
    NOT
    NULL,
    appt_datetime
    DATETIME
(
    6
) NOT NULL,
    status VARCHAR
(
    30
) NOT NULL,
    notes TEXT,
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT fk_appointments_patient FOREIGN KEY
(
    patient_id
) REFERENCES patients
(
    id
),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY
(
    doctor_id
) REFERENCES doctors
(
    id
),
    INDEX idx_appointments_patient_id
(
    patient_id
),
    INDEX idx_appointments_doctor_id
(
    doctor_id
),
    INDEX idx_appointments_appt_datetime
(
    appt_datetime
),
    INDEX idx_appointments_conflict_check
(
    doctor_id, appt_datetime, status
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS medical_records
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    patient_id
    BIGINT
    NOT
    NULL,
    doctor_id
    BIGINT
    NOT
    NULL,
    appointment_id
    BIGINT,
    diagnosis
    TEXT,
    visit_date
    DATE
    NOT
    NULL,
    notes
    TEXT,
    created_at
    DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT uk_medical_records_appointment UNIQUE
(
    appointment_id
),
    CONSTRAINT fk_medical_records_patient FOREIGN KEY
(
    patient_id
) REFERENCES patients
(
    id
),
    CONSTRAINT fk_medical_records_doctor FOREIGN KEY
(
    doctor_id
) REFERENCES doctors
(
    id
),
    CONSTRAINT fk_medical_records_appointment FOREIGN KEY
(
    appointment_id
) REFERENCES appointments
(
    id
),
    INDEX idx_medical_records_patient_id
(
    patient_id
),
    INDEX idx_medical_records_doctor_id
(
    doctor_id
),
    INDEX idx_medical_records_visit_date
(
    visit_date
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prescriptions
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    medical_record_id
    BIGINT
    NOT
    NULL,
    doctor_id
    BIGINT
    NOT
    NULL,
    issued_date
    DATE
    NOT
    NULL,
    status
    VARCHAR
(
    20
) NOT NULL,
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT fk_prescriptions_medical_record FOREIGN KEY
(
    medical_record_id
) REFERENCES medical_records
(
    id
),
    CONSTRAINT fk_prescriptions_doctor FOREIGN KEY
(
    doctor_id
) REFERENCES doctors
(
    id
),
    INDEX idx_prescriptions_medical_record_id
(
    medical_record_id
),
    INDEX idx_prescriptions_doctor_id
(
    doctor_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS medicines
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    medicine_name
    VARCHAR
(
    150
) NOT NULL,
    generic_name VARCHAR
(
    150
),
    category VARCHAR
(
    80
),
    unit VARCHAR
(
    30
),
    unit_price DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0,
    insurance_covered BIT
(
    1
) NOT NULL DEFAULT b'0',
    active BIT
(
    1
) NOT NULL DEFAULT b'1',
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    INDEX idx_medicines_medicine_name
(
    medicine_name
),
    INDEX idx_medicines_category
(
    category
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prescription_items
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    prescription_id
    BIGINT
    NOT
    NULL,
    medicine_id
    BIGINT
    NOT
    NULL,
    quantity
    INT
    NOT
    NULL,
    dosage
    VARCHAR
(
    120
),
    instructions TEXT,
    unit_price_at_time DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0,
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT fk_prescription_items_prescription FOREIGN KEY
(
    prescription_id
) REFERENCES prescriptions
(
    id
),
    CONSTRAINT fk_prescription_items_medicine FOREIGN KEY
(
    medicine_id
) REFERENCES medicines
(
    id
),
    INDEX idx_prescription_items_prescription_id
(
    prescription_id
),
    INDEX idx_prescription_items_medicine_id
(
    medicine_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lab_tests
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    medical_record_id
    BIGINT
    NOT
    NULL,
    test_type
    VARCHAR
(
    120
) NOT NULL,
    ordered_by BIGINT NOT NULL,
    result TEXT,
    result_file_url VARCHAR
(
    255
),
    status VARCHAR
(
    30
) NOT NULL,
    test_date DATETIME
(
    6
),
    fee DECIMAL
(
    12,
    2
) DEFAULT 0,
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT fk_lab_tests_medical_record FOREIGN KEY
(
    medical_record_id
) REFERENCES medical_records
(
    id
),
    CONSTRAINT fk_lab_tests_ordered_by FOREIGN KEY
(
    ordered_by
) REFERENCES doctors
(
    id
),
    INDEX idx_lab_tests_medical_record_id
(
    medical_record_id
),
    INDEX idx_lab_tests_ordered_by
(
    ordered_by
),
    INDEX idx_lab_tests_test_date
(
    test_date
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoices
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    medical_record_id
    BIGINT
    NOT
    NULL,
    patient_id
    BIGINT
    NOT
    NULL,
    examination_fee
    DECIMAL
(
    14,
    2
) DEFAULT 0,
    medicine_fee DECIMAL
(
    14,
    2
) DEFAULT 0,
    lab_fee DECIMAL
(
    14,
    2
) DEFAULT 0,
    total_amount DECIMAL
(
    14,
    2
),
    insurance_coverage VARCHAR
(
    20
),
    insurance_amount DECIMAL
(
    14,
    2
),
    paid_amount DECIMAL
(
    14,
    2
),
    payment_method VARCHAR
(
    30
),
    status VARCHAR
(
    20
) NOT NULL,
    paid_at DATETIME
(
    6
),
    notes TEXT,
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT uk_invoices_medical_record UNIQUE
(
    medical_record_id
),
    CONSTRAINT fk_invoices_medical_record FOREIGN KEY
(
    medical_record_id
) REFERENCES medical_records
(
    id
),
    CONSTRAINT fk_invoices_patient FOREIGN KEY
(
    patient_id
) REFERENCES patients
(
    id
),
    INDEX idx_invoices_patient_id
(
    patient_id
),
    INDEX idx_invoices_status
(
    status
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    username
    VARCHAR
(
    80
) NOT NULL,
    password_hash VARCHAR
(
    255
) NOT NULL,
    email VARCHAR
(
    120
),
    full_name VARCHAR
(
    150
) NOT NULL,
    is_active BIT NOT NULL,
    doctor_id BIGINT,
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT uk_users_username UNIQUE
(
    username
),
    CONSTRAINT fk_users_doctor FOREIGN KEY
(
    doctor_id
) REFERENCES doctors
(
    id
),
    INDEX idx_users_username
(
    username
),
    INDEX idx_users_doctor_id
(
    doctor_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    role_name
    VARCHAR
(
    30
) NOT NULL,
    created_at DATETIME
(
    6
) NOT NULL,
    updated_at DATETIME
(
    6
) NOT NULL,
    CONSTRAINT uk_roles_role_name UNIQUE
(
    role_name
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id
    BIGINT
    NOT
    NULL,
    role_id
    BIGINT
    NOT
    NULL,
    PRIMARY
    KEY
(
    user_id,
    role_id
),
    CONSTRAINT fk_user_roles_user FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
),
    CONSTRAINT fk_user_roles_role FOREIGN KEY
(
    role_id
) REFERENCES roles
(
    id
),
    INDEX idx_user_roles_role_id
(
    role_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    user_id
    BIGINT,
    action
    VARCHAR
(
    80
) NOT NULL,
    entity_type VARCHAR
(
    120
),
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR
(
    45
),
    created_at DATETIME
(
    6
) NOT NULL,
    INDEX idx_audit_logs_user_id
(
    user_id
),
    INDEX idx_audit_logs_entity
(
    entity_type,
    entity_id
),
    INDEX idx_audit_logs_created_at
(
    created_at
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci;
