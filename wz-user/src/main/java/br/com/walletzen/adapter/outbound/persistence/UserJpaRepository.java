package br.com.walletzen.adapter.outbound.persistence;

import br.com.walletzen.adapter.outbound.persistence.entities.UserEntity;
import br.com.walletzen.core.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

    @Query("SELECT u FROM UserEntity u WHERE u.recordStatus = true")
    Page<UserEntity> findAllActiveUsers(Pageable pageable);

    @Query("SELECT u FROM UserEntity u WHERE u.id = :userId AND u.recordStatus = true")
    Optional<UserEntity> findUserById(UUID userId);
}
