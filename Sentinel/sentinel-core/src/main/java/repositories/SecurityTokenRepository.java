package repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import model.entities.BaseSecurityToken;
import model.entities.account.BaseAccount;

public interface SecurityTokenRepository extends JpaRepository<BaseSecurityToken, UUID> {
	
	Optional<BaseSecurityToken> findByAccessToken(UUID token);
	Optional<BaseSecurityToken> findByRefreshToken(UUID token);
	Optional<BaseSecurityToken> findByAccount(BaseAccount account);

}
