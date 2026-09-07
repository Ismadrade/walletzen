-- Outbox pattern (Fase 4 / plano 08): o evento UserDeleted é gravado nesta tabela
-- na MESMA transação do soft-delete; um poller publica no Kafka e marca published_at.
-- id bigint identity = ordem de publicação; event_id uuid = chave de negócio / idempotência.

create table outbox_event (
    id             bigint        generated always as identity,
    event_id       uuid          not null,
    aggregate_type varchar(64)   not null,
    aggregate_id   varchar(64)   not null,
    event_type     varchar(64)   not null,
    topic          varchar(128)  not null,
    payload        text          not null,
    created_at     timestamp(6)  not null,
    published_at   timestamp(6),
    attempts       integer       not null default 0,
    last_error     text,
    constraint pk_outbox_event primary key (id),
    constraint uk_outbox_event_event_id unique (event_id)
);

create index idx_outbox_event_unpublished on outbox_event (id) where published_at is null;
