package repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import model.entities.BaseAccount;
import model.entities.BaseSecurityToken;

public interface BaseSecurityTokenRepository extends JpaRepository<BaseSecurityToken, UUID> {
	
	Optional<BaseSecurityToken> findByAccessToken(UUID token);
	Optional<BaseSecurityToken> findByRefreshToken(UUID token);
	Optional<BaseSecurityToken> findByAccount(BaseAccount account);

}
