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
