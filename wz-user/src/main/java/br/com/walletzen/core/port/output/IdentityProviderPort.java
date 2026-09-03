package br.com.walletzen.core.port.output;

/**
 * Provisiona a identidade de login do usuário no Identity Provider (Keycloak).
 * O {@code wz-user} é dono do cadastro completo — pessoa + login.
 */
public interface IdentityProviderPort {

    /**
     * Cria o usuário no IdP com a senha informada e a role padrão {@code USER}.
     *
     * @return o id do usuário no IdP (a ser guardado em {@code wz_user.keycloak_id})
     */
    String createUser(String email, String firstName, String lastName, String rawPassword);

    /** Propaga email/nome para o IdP. Best-effort. */
    void updateUser(String identityId, String email, String firstName, String lastName);

    /** Soft delete: desabilita o usuário no IdP ({@code enabled=false}). Best-effort. */
    void disableUser(String identityId);

    /** Remove o usuário do IdP. Usado só para compensar um create que falhou depois. */
    void deleteUser(String identityId);
}
