DROP TABLE IF EXISTS public.WZ_USER;

CREATE TABLE public.WZ_USER (
    ID UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    NAME VARCHAR NOT NULL,
    CPF VARCHAR UNIQUE NOT NULL,
    EMAIL VARCHAR UNIQUE NOT NULL,
    BIRTH_DATE TIMESTAMP,
    CREATED_AT TIMESTAMP NOT NULL DEFAULT NOW(),
    UPDATED_AT TIMESTAMP,
    RECORD_STATUS BOOLEAN
);


INSERT INTO public.WZ_USER (ID, NAME, CPF, EMAIL, BIRTH_DATE, CREATED_AT, UPDATED_AT, RECORD_STATUS) VALUES
(gen_random_uuid(), 'Ismael da Silva de Andrade', '62167159927', 'ismael_andrade@gteste.com.br', '1993-09-09 00:00:00', NOW(), NOW(), true),
(gen_random_uuid(), 'João Rafael Cardoso de Medeiros', '12205751948', 'joao_rafael@gteste.com.br', '1993-09-01 00:00:00', NOW(), NOW(), true);