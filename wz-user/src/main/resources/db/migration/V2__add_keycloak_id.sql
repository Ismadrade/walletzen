-- Vínculo com o usuário do Keycloak (provisionado no POST /users/).
-- Null para linhas legadas; único quando presente.
alter table wz_user add column keycloak_id varchar(255);
create unique index uk_wz_user_keycloak_id on wz_user (keycloak_id);
