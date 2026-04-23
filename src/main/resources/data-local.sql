-- Local development only: seed a local admin account.
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
ON CONFLICT (email) DO NOTHING;

-- Local development only: seed patient accounts with visible (unhashed) passwords.
-- Credentials:
--  - john.doe@traumateam.com / patient123
--  - jane.smith@traumateam.com / patient123
--  - anders.andersson@traumateam.com / patient123
-- Note: "{noop}" means "do not hash" in Spring Security.
INSERT INTO user_account (id, email, password_hash, role, provider, enabled, created_at)
VALUES (
           '550e8400-e29b-41d4-a716-446655440000',
           'john.doe@traumateam.com',
           '{noop}patient123',
           'PATIENT',
           'LOCAL',
           true,
           NOW()
       )
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_account (id, email, password_hash, role, provider, enabled, created_at)
VALUES (
           '550e8400-e29b-41d4-a716-446655440001',
           'jane.smith@traumateam.com',
           '{noop}patient123',
           'PATIENT',
           'LOCAL',
           true,
           NOW()
       )
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_account (id, email, password_hash, role, provider, enabled, created_at)
VALUES (
           '550e8400-e29b-41d4-a716-446655440002',
           'anders.andersson@traumateam.com',
           '{noop}patient123',
           'PATIENT',
           'LOCAL',
           true,
           NOW()
       )
ON CONFLICT (email) DO NOTHING;
