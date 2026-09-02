package br.com.walletzen.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Extrai as roles de realm do Keycloak ({@code realm_access.roles}) e as expõe
 * como authorities {@code ROLE_*} — o Spring não faz esse mapeamento sozinho.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of();
        }
        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
