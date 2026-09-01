-- Baseline: gerado a partir do DDL do Hibernate (entidade Transaction).
-- A partir daqui o schema é evoluído só por migrations; ddl-auto = validate.

create table wz_transaction (
    id               uuid          not null,
    transaction_type varchar(255)  not null,
    amount           numeric(10, 2) not null,
    description      varchar(255),
    user_id          uuid          not null,
    created_at       timestamp(6)  not null,
    updated_at       timestamp(6)  not null,
    record_status    boolean       not null,
    constraint pk_wz_transaction primary key (id),
    constraint ck_wz_transaction_type check (transaction_type in ('INCOME', 'EXPENSE'))
);

create index idx_wz_transaction_user_created on wz_transaction (user_id, created_at);
