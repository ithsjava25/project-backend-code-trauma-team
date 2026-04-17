-- Seed Patients
INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'John', 'Doe', '19850512-1234', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'Jane', 'Smith', '19920824-5678', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440002', 'Anders', 'Andersson', '19780315-9012', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Seed Employees
INSERT INTO employees (id, display_name, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440000', 'Admin User', 'MANAGER', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, display_name, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440001', 'Dr. Alice Roberts', 'DOCTOR', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, display_name, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440002', 'Nurse Bob Jones', 'NURSE', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Seed Cases
INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440000', 'Acute Chest Pain', 'Patient arrived with severe chest pain and shortness of breath.', 'OPEN', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440001', 'Follow-up: Fracture', 'Routine follow-up for a healed radial fracture.', 'OPEN', '550e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Creates the new table for all user accounts (both local and GitHub)
CREATE TABLE IF NOT EXISTS user_account (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255),
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
    );

-- Add a local admin user with the role MANAGER and password 'password'
-- This replaces the old in-memory user
INSERT INTO user_account (id, email, password_hash, role, provider, enabled, created_at)
VALUES (
           'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
           'admin@traumateam.com',
           '$2a$10$mOvf0LGTHyTmRAcf2l.KPu8nq7arTJTjOizhm/i9jmx5sXLIwGOAK',
           'MANAGER',
           'LOCAL',
           true,
           NOW()
       )
ON CONFLICT (email) DO UPDATE SET
    password_hash = '$2a$10$mOvf0LGTHyTmRAcf2l.KPu8nq7arTJTjOizhm/i9jmx5sXLIwGOAK';
