package br.com.walletzen.authuser.adapters.out.repository;

import br.com.walletzen.authuser.adapters.out.repository.entity.UserModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserModelRepository extends JpaRepository<UserModelEntity, UUID> {
}
