-- Seed Patients
INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'John', 'Doe', '19850512-1234', CURRENT_TIMESTAMP);

INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'Jane', 'Smith', '19920824-5678', CURRENT_TIMESTAMP);

INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440002', 'Anders', 'Andersson', '19780315-9012', CURRENT_TIMESTAMP);

-- Seed Employees
INSERT INTO employees (id, display_name, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440000', 'Admin User', 'ADMIN', CURRENT_TIMESTAMP);

INSERT INTO employees (id, display_name, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440001', 'Dr. Alice Roberts', 'CASE_OWNER', CURRENT_TIMESTAMP);

INSERT INTO employees (id, display_name, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440002', 'Nurse Bob Jones', 'HANDLER', CURRENT_TIMESTAMP);

-- Seed Cases
INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440000', 'Acute Chest Pain', 'Patient arrived with severe chest pain and shortness of breath.', 'OPEN', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP);

INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440001', 'Follow-up: Fracture', 'Routine follow-up for a healed radial fracture.', 'OPEN', '550e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP);
