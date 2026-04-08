-- 1. Seed Accounts (Identity & Role)
INSERT INTO accounts (id, github_username, display_name, role, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440000', NULL, 'John Doe', 'PATIENT', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440001', NULL, 'Jane Smith', 'PATIENT', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', NULL, 'Anders Andersson', 'PATIENT', CURRENT_TIMESTAMP),
('770e8400-e29b-41d4-a716-446655440000', 'admin', 'Admin User', 'MANAGER', CURRENT_TIMESTAMP),
('770e8400-e29b-41d4-a716-446655440001', 'alice', 'Dr. Alice Roberts', 'DOCTOR', CURRENT_TIMESTAMP),
('770e8400-e29b-41d4-a716-446655440002', 'bob', 'Nurse Bob Jones', 'NURSE', CURRENT_TIMESTAMP),
('880e8400-e29b-41d4-a716-446655440000', 'newuser', 'New GitHub User', 'PENDING', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 2. Seed Patient Profiles (Specific Data)
INSERT INTO patient_profiles (id, first_name, last_name, personal_identity_number) VALUES 
('550e8400-e29b-41d4-a716-446655440000', 'John', 'Doe', '19850512-1234'),
('550e8400-e29b-41d4-a716-446655440001', 'Jane', 'Smith', '19920824-5678'),
('550e8400-e29b-41d4-a716-446655440002', 'Anders', 'Andersson', '19780315-9012')
ON CONFLICT (id) DO NOTHING;

-- 3. Seed Employee Profiles (Specific Data)
INSERT INTO employee_profiles (id, employee_number) VALUES 
('770e8400-e29b-41d4-a716-446655440000', 'EMP-001'),
('770e8400-e29b-41d4-a716-446655440001', 'EMP-002'),
('770e8400-e29b-41d4-a716-446655440002', 'EMP-003')
ON CONFLICT (id) DO NOTHING;

-- 4. Seed Cases (References Patient Profile ID)
INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440000', 'Acute Chest Pain', 'Patient arrived with severe chest pain.', 'OPEN', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
