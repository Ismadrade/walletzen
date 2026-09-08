-- Idempotência do consumidor (Fase 4 / plano 08, Etapa 2): registra qual eventId já
-- foi processado por qual consumidor. Como a entrega é at-least-once, a mesma mensagem
-- pode chegar duas vezes; o handler consulta esta tabela antes de aplicar o efeito.

create table processed_event (
    event_id     uuid          not null,
    consumer     varchar(64)   not null,
    processed_at timestamp(6)  not null,
    constraint pk_processed_event primary key (event_id, consumer)
);
