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
    (gen_random_uuid(), 'INCOME', 'Salário mensal', 5000.00, 'cfb0065b-4f08-422e-a369-c24d0111e85c', NOW(), NOW(), true),
    (gen_random_uuid(), 'INCOME', 'Freelance de desenvolvimento', 1200.50, 'cfb0065b-4f08-422e-a369-c24d0111e85c', NOW(), NOW(), true),
    (gen_random_uuid(), 'EXPENSE', 'Aluguel do apartamento', 1800.00, 'cfb0065b-4f08-422e-a369-c24d0111e85c', NOW(), NOW(), true),
    (gen_random_uuid(), 'EXPENSE', 'Conta de luz', 220.75, 'cfb0065b-4f08-422e-a369-c24d0111e85c', NOW(), NOW(), true);
