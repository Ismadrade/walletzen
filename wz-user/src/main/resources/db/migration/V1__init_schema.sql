-- Baseline: gerado a partir do DDL do Hibernate (entidade UserEntity).
-- A partir daqui o schema é evoluído só por migrations; ddl-auto = validate.

create table wz_user (
    id            uuid         not null,
    name          varchar(255) not null,
    cpf           varchar(255) not null,
    email         varchar(255) not null,
    birth_date    date,
    created_at    timestamp(6) not null,
    updated_at    timestamp(6),
    record_status boolean,
    constraint pk_wz_user primary key (id),
    constraint uk_wz_user_cpf unique (cpf),
    constraint uk_wz_user_email unique (email)
);
