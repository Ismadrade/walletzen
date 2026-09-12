-- Data do lançamento (competência), escolhida pelo usuário. Até aqui a listagem
-- usava created_at, que é só o instante técnico em que a linha foi gravada.

alter table wz_transaction add column transaction_date date;

-- lançamentos existentes: a data passa a ser o dia em que foram gravados
update wz_transaction set transaction_date = cast(created_at as date);

alter table wz_transaction alter column transaction_date set not null;

-- a listagem agora filtra por (user_id, transaction_date); o índice antigo deixou de servir
drop index idx_wz_transaction_user_created;
create index idx_wz_transaction_user_date on wz_transaction (user_id, transaction_date);
