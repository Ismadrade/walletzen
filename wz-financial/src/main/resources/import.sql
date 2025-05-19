DROP TABLE IF EXISTS public.WZ_TRANSACTION;

CREATE TABLE public.WZ_TRANSACTION (
    ID UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    TRANSACTION_TYPE VARCHAR NOT NULL,
    DESCRIPTION VARCHAR,
    AMOUNT DECIMAL(10,2) NOT NULL,
    USER_ID UUID NOT NULL,
    CREATED_AT TIMESTAMP NOT NULL DEFAULT NOW(),
    UPDATED_AT TIMESTAMP,
    RECORD_STATUS BOOLEAN
);

INSERT INTO public.WZ_TRANSACTION (ID, TRANSACTION_TYPE, DESCRIPTION, AMOUNT, USER_ID, CREATED_AT, UPDATED_AT, RECORD_STATUS)
VALUES
    (gen_random_uuid(), 'INCOME', 'Salário mensal', 5000.00, '03d7a869-fa32-4b01-986d-f8f2d7fc5fdb', NOW(), NOW(), true),
    (gen_random_uuid(), 'INCOME', 'Freelance de desenvolvimento', 1200.50, '03d7a869-fa32-4b01-986d-f8f2d7fc5fdb', NOW(), NOW(), true),
    (gen_random_uuid(), 'EXPENSE', 'Aluguel do apartamento', 1800.00, '03d7a869-fa32-4b01-986d-f8f2d7fc5fdb', NOW(), NOW(), true),
    (gen_random_uuid(), 'EXPENSE', 'Conta de luz', 220.75, '03d7a869-fa32-4b01-986d-f8f2d7fc5fdb', NOW(), NOW(), true),
    (gen_random_uuid(), 'INCOME', 'Salário mensal', 3000.00, '986cd6ed-c329-4341-9162-429bdb33c10f', NOW(), NOW(), true),
    (gen_random_uuid(), 'INCOME', 'PLR', 525.49, '986cd6ed-c329-4341-9162-429bdb33c10f', NOW(), NOW(), true),
    (gen_random_uuid(), 'EXPENSE', 'Cartão de Credito NuBank', 800.00, '986cd6ed-c329-4341-9162-429bdb33c10f', NOW(), NOW(), true),
    (gen_random_uuid(), 'EXPENSE', 'Cartão de Credito Bradesco', 220.75, '986cd6ed-c329-4341-9162-429bdb33c10f', NOW(), NOW(), true);
