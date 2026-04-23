-- ------------------------------------------------------------
-- Core user accounts (shared seed)
--
-- ------------------------------------------------------------
-- Uses GITHUB provider to avoid shipping known local credentials in shared data.
INSERT INTO user_account (id, email, password_hash, role, provider, provider_user_id, enabled, created_at)
VALUES ('120e8400-e29b-41d4-a716-446655440000', 'admin.seed@traumateam.local', NULL, 'MANAGER', 'GITHUB', 'seed-admin-001', true, CURRENT_TIMESTAMP - INTERVAL '45 days')
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_account (id, email, password_hash, role, provider, provider_user_id, enabled, created_at)
VALUES ('120e8400-e29b-41d4-a716-446655440001', 'doctor.seed@traumateam.local', NULL, 'DOCTOR', 'GITHUB', 'seed-doctor-001', true, CURRENT_TIMESTAMP - INTERVAL '42 days')
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_account (id, email, password_hash, role, provider, provider_user_id, enabled, created_at)
VALUES ('120e8400-e29b-41d4-a716-446655440002', 'patient.seed@traumateam.local', NULL, 'PATIENT', 'GITHUB', 'seed-patient-001', true, CURRENT_TIMESTAMP - INTERVAL '40 days')
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_account (id, email, password_hash, role, provider, provider_user_id, enabled, created_at)
VALUES ('228023f0-e676-356e-ba78-097cc894cc5a', NULL, NULL, 'MANAGER', 'GITHUB', 'linuswestling', true, CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_account (id, email, password_hash, role, provider, provider_user_id, enabled, created_at)
VALUES ('876a23e4-2207-3e12-b36c-c44e4a50f772', NULL, NULL, 'MANAGER', 'GITHUB', 'mattknatt', true, CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- Patients
-- ------------------------------------------------------------
INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'John', 'Doe', '19850512-1234', CURRENT_TIMESTAMP - INTERVAL '30 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'Jane', 'Smith', '19920824-5678', CURRENT_TIMESTAMP - INTERVAL '28 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440002', 'Anders', 'Andersson', '19780315-9012', CURRENT_TIMESTAMP - INTERVAL '27 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440003', 'Maria', 'Nilsson', '19951201-7731', CURRENT_TIMESTAMP - INTERVAL '25 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440004', 'Erik', 'Larsson', '19691109-1942', CURRENT_TIMESTAMP - INTERVAL '24 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, first_name, last_name, personal_identity_number, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440005', 'Sofia', 'Berg', '20010218-3307', CURRENT_TIMESTAMP - INTERVAL '20 days')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- Employees
-- ------------------------------------------------------------
INSERT INTO employees (id, display_name, github_username, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440000', 'Admin User', 'seed-admin', 'MANAGER', CURRENT_TIMESTAMP - INTERVAL '45 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, display_name, github_username, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440001', 'Dr. Alice Roberts', 'alice-roberts-md', 'DOCTOR', CURRENT_TIMESTAMP - INTERVAL '42 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, display_name, github_username, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440002', 'Nurse Bob Jones', 'bob-jones-rn', 'NURSE', CURRENT_TIMESTAMP - INTERVAL '39 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, display_name, github_username, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440003', 'Dr. Sara Lindholm', 'sara-lindholm-md', 'DOCTOR', CURRENT_TIMESTAMP - INTERVAL '35 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, display_name, github_username, role, created_at)
VALUES ('770e8400-e29b-41d4-a716-446655440004', 'Nurse Emma Karlsson', 'emma-karlsson-rn', 'NURSE', CURRENT_TIMESTAMP - INTERVAL '33 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, display_name, github_username, role, created_at)
VALUES ('228023f0-e676-356e-ba78-097cc894cc5a', 'Linus Westling', 'linuswestling', 'MANAGER', CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, display_name, github_username, role, created_at)
VALUES ('876a23e4-2207-3e12-b36c-c44e4a50f772', 'Matt Knatt', 'mattknatt', 'MANAGER', CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- Cases
-- ------------------------------------------------------------
INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440000', 'Acute Chest Pain',
        'Patient arrived with severe chest pain and shortness of breath during exercise.',
        'COMMUNICATION', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440001',
        '770e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '12 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440001', 'Follow-up: Fracture',
        'Routine follow-up after distal radius fracture. Verify mobility and pain level.',
        'ASSIGNED', '550e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001',
        '770e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '11 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440002', 'Persistent Migraine',
        'Recurring migraine episodes for two weeks with light sensitivity and nausea.',
        'UPDATED', '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440003',
        '770e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '10 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440003', 'Post-surgery Wound Check',
        'Follow-up wound inspection after appendectomy; monitor healing and infection markers.',
        'CLOSED', '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440001',
        '770e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '9 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440004', 'Hypertension Medication Review',
        'Blood pressure fluctuations despite current medication. Evaluate treatment adjustment.',
        'COMMUNICATION', '550e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440003',
        '770e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '8 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440005', 'Allergic Reaction Observation',
        'Mild allergic reaction after new antibiotic; monitor symptoms and response to treatment.',
        'CREATED', '550e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440001',
        '770e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440006', 'Knee Pain After Sports Injury',
        'Suspected meniscus strain after football practice; reduced range of motion.',
        'ASSIGNED', '550e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440003',
        '770e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cases (id, title, description, status, patient_id, owner_id, handler_id, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440007', 'Diabetes Checkup and Education',
        'Quarterly diabetes review with glucose trends, lifestyle coaching and medication adherence.',
        'UPDATED', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440001',
        '770e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '1 days')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- Case notes (comments)
-- ------------------------------------------------------------
INSERT INTO case_notes (id, case_id, content, author_display_name, author_github_username, author_role, created_at)
VALUES ('a90e8400-e29b-41d4-a716-446655440000', '990e8400-e29b-41d4-a716-446655440000',
        'ECG completed. Awaiting blood panel for troponin and CRP.',
        'Dr. Alice Roberts', 'alice-roberts-md', 'DOCTOR', CURRENT_TIMESTAMP - INTERVAL '12 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO case_notes (id, case_id, content, author_display_name, author_github_username, author_role, created_at)
VALUES ('a90e8400-e29b-41d4-a716-446655440001', '990e8400-e29b-41d4-a716-446655440000',
        'Patient stable on observation ward. Chest pain now rated 3/10.',
        'Nurse Bob Jones', 'bob-jones-rn', 'NURSE', CURRENT_TIMESTAMP - INTERVAL '11 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO case_notes (id, case_id, content, author_display_name, author_github_username, author_role, created_at)
VALUES ('a90e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440001',
        'Cast removed. Physiotherapy exercises explained and demonstrated.',
        'Dr. Alice Roberts', 'alice-roberts-md', 'DOCTOR', CURRENT_TIMESTAMP - INTERVAL '10 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO case_notes (id, case_id, content, author_display_name, author_github_username, author_role, created_at)
VALUES ('a90e8400-e29b-41d4-a716-446655440003', '990e8400-e29b-41d4-a716-446655440002',
        'Triggered by stress and lack of sleep. Recommended headache diary.',
        'Dr. Sara Lindholm', 'sara-lindholm-md', 'DOCTOR', CURRENT_TIMESTAMP - INTERVAL '9 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO case_notes (id, case_id, content, author_display_name, author_github_username, author_role, created_at)
VALUES ('a90e8400-e29b-41d4-a716-446655440004', '990e8400-e29b-41d4-a716-446655440003',
        'Wound dry and clean. Sutures removed. No signs of infection.',
        'Nurse Emma Karlsson', 'emma-karlsson-rn', 'NURSE', CURRENT_TIMESTAMP - INTERVAL '8 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO case_notes (id, case_id, content, author_display_name, author_github_username, author_role, created_at)
VALUES ('a90e8400-e29b-41d4-a716-446655440005', '990e8400-e29b-41d4-a716-446655440004',
        'Home blood-pressure log reviewed. Evening values remain elevated.',
        'Dr. Sara Lindholm', 'sara-lindholm-md', 'DOCTOR', CURRENT_TIMESTAMP - INTERVAL '7 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO case_notes (id, case_id, content, author_display_name, author_github_username, author_role, created_at)
VALUES ('a90e8400-e29b-41d4-a716-446655440006', '990e8400-e29b-41d4-a716-446655440006',
        'Swelling reduced. MRI referral submitted for meniscus evaluation.',
        'Dr. Sara Lindholm', 'sara-lindholm-md', 'DOCTOR', CURRENT_TIMESTAMP - INTERVAL '1 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO case_notes (id, case_id, content, author_display_name, author_github_username, author_role, created_at)
VALUES ('a90e8400-e29b-41d4-a716-446655440007', '990e8400-e29b-41d4-a716-446655440007',
        'Diet plan and glucose monitoring schedule reviewed with patient.',
        'Nurse Bob Jones', 'bob-jones-rn', 'NURSE', CURRENT_TIMESTAMP - INTERVAL '20 hours')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- Documents
-- ------------------------------------------------------------
INSERT INTO documents (id, file_name, s3_key, content_type, file_size, uploaded_at, uploaded_by, case_id)
VALUES ('b90e8400-e29b-41d4-a716-446655440000', 'ecg-initial.pdf', 'cases/990e8400-e29b-41d4-a716-446655440000/ecg-initial.pdf',
        'application/pdf', 238412, CURRENT_TIMESTAMP - INTERVAL '12 days', '770e8400-e29b-41d4-a716-446655440001',
        '990e8400-e29b-41d4-a716-446655440000')
ON CONFLICT (id) DO NOTHING;

INSERT INTO documents (id, file_name, s3_key, content_type, file_size, uploaded_at, uploaded_by, case_id)
VALUES ('b90e8400-e29b-41d4-a716-446655440001', 'xray-fracture-followup.png', 'cases/990e8400-e29b-41d4-a716-446655440001/xray-fracture-followup.png',
        'image/png', 484190, CURRENT_TIMESTAMP - INTERVAL '10 days', '770e8400-e29b-41d4-a716-446655440002',
        '990e8400-e29b-41d4-a716-446655440001')
ON CONFLICT (id) DO NOTHING;

INSERT INTO documents (id, file_name, s3_key, content_type, file_size, uploaded_at, uploaded_by, case_id)
VALUES ('b90e8400-e29b-41d4-a716-446655440002', 'migraine-diary-week1.docx', 'cases/990e8400-e29b-41d4-a716-446655440002/migraine-diary-week1.docx',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 91324,
        CURRENT_TIMESTAMP - INTERVAL '8 days', '770e8400-e29b-41d4-a716-446655440004', '990e8400-e29b-41d4-a716-446655440002')
ON CONFLICT (id) DO NOTHING;

INSERT INTO documents (id, file_name, s3_key, content_type, file_size, uploaded_at, uploaded_by, case_id)
VALUES ('b90e8400-e29b-41d4-a716-446655440003', 'post-op-wound-photo.jpg', 'cases/990e8400-e29b-41d4-a716-446655440003/post-op-wound-photo.jpg',
        'image/jpeg', 356778, CURRENT_TIMESTAMP - INTERVAL '9 days', '770e8400-e29b-41d4-a716-446655440004',
        '990e8400-e29b-41d4-a716-446655440003')
ON CONFLICT (id) DO NOTHING;

INSERT INTO documents (id, file_name, s3_key, content_type, file_size, uploaded_at, uploaded_by, case_id)
VALUES ('b90e8400-e29b-41d4-a716-446655440004', 'bp-home-readings.xlsx', 'cases/990e8400-e29b-41d4-a716-446655440004/bp-home-readings.xlsx',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 120512,
        CURRENT_TIMESTAMP - INTERVAL '6 days', '770e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440004')
ON CONFLICT (id) DO NOTHING;

INSERT INTO documents (id, file_name, s3_key, content_type, file_size, uploaded_at, uploaded_by, case_id)
VALUES ('b90e8400-e29b-41d4-a716-446655440005', 'mri-referral.pdf', 'cases/990e8400-e29b-41d4-a716-446655440006/mri-referral.pdf',
        'application/pdf', 204889, CURRENT_TIMESTAMP - INTERVAL '22 hours', '770e8400-e29b-41d4-a716-446655440003',
        '990e8400-e29b-41d4-a716-446655440006')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- Audit events
-- ------------------------------------------------------------
INSERT INTO audit_events (id, occurred_at, actor_id, actor_role, principal_name, request_path, query_string, handler,
                          response_status, error_type, case_id, status_change, event_name, description, client_ip, user_agent)
VALUES ('c90e8400-e29b-41d4-a716-446655440000', CURRENT_TIMESTAMP - INTERVAL '12 days',
        '770e8400-e29b-41d4-a716-446655440001', 'DOCTOR', 'doctor.seed@traumateam.local',
        '/ui/cases/990e8400-e29b-41d4-a716-446655440000', NULL, 'CaseUiController#showCase',
        200, NULL, '990e8400-e29b-41d4-a716-446655440000', 'CREATED->ASSIGNED', 'CASE_STATUS_CHANGED',
        'Case owner assigned to Dr. Alice Roberts.', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)')
ON CONFLICT (id) DO NOTHING;

INSERT INTO audit_events (id, occurred_at, actor_id, actor_role, principal_name, request_path, query_string, handler,
                          response_status, error_type, case_id, status_change, event_name, description, client_ip, user_agent)
VALUES ('c90e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP - INTERVAL '11 days',
        '770e8400-e29b-41d4-a716-446655440002', 'NURSE', 'nurse.bob@traumateam.local',
        '/ui/cases/990e8400-e29b-41d4-a716-446655440000/comments', NULL, 'CaseNoteUiController#create',
        302, NULL, '990e8400-e29b-41d4-a716-446655440000', NULL, 'CASE_NOTE_ADDED',
        'Nurse added observation note after first medication cycle.', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64)')
ON CONFLICT (id) DO NOTHING;

INSERT INTO audit_events (id, occurred_at, actor_id, actor_role, principal_name, request_path, query_string, handler,
                          response_status, error_type, case_id, status_change, event_name, description, client_ip, user_agent)
VALUES ('c90e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '10 days',
        '770e8400-e29b-41d4-a716-446655440001', 'DOCTOR', 'doctor.seed@traumateam.local',
        '/ui/cases/990e8400-e29b-41d4-a716-446655440001/documents', NULL, 'DocumentUiController#upload',
        201, NULL, '990e8400-e29b-41d4-a716-446655440001', NULL, 'DOCUMENT_UPLOADED',
        'Follow-up x-ray uploaded and attached to case timeline.', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64)')
ON CONFLICT (id) DO NOTHING;

INSERT INTO audit_events (id, occurred_at, actor_id, actor_role, principal_name, request_path, query_string, handler,
                          response_status, error_type, case_id, status_change, event_name, description, client_ip, user_agent)
VALUES ('c90e8400-e29b-41d4-a716-446655440003', CURRENT_TIMESTAMP - INTERVAL '8 days',
        '770e8400-e29b-41d4-a716-446655440003', 'DOCTOR', 'doctor.sara@traumateam.local',
        '/api/cases/990e8400-e29b-41d4-a716-446655440004/status', 'target=COMMUNICATION', 'CaseController#updateStatus',
        200, NULL, '990e8400-e29b-41d4-a716-446655440004', 'ASSIGNED->COMMUNICATION', 'CASE_STATUS_CHANGED',
        'Case moved to communication after medication discussion with patient.', '127.0.0.1',
        'PostmanRuntime/7.43.0')
ON CONFLICT (id) DO NOTHING;

INSERT INTO audit_events (id, occurred_at, actor_id, actor_role, principal_name, request_path, query_string, handler,
                          response_status, error_type, case_id, status_change, event_name, description, client_ip, user_agent)
VALUES ('c90e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '2 days',
        '770e8400-e29b-41d4-a716-446655440000', 'MANAGER', 'admin.seed@traumateam.local',
        '/ui/admin/audit', NULL, 'AdminAuditUiController#index',
        200, NULL, NULL, NULL, 'AUDIT_VIEWED',
        'Manager reviewed audit overview dashboard.', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64)')
ON CONFLICT (id) DO NOTHING;
