package repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import model.entities.BaseAccount;

public interface BaseAccountRepository extends JpaRepository<BaseAccount, UUID> {

	boolean existsByUsernameAndPassword(String username, String password);
	boolean existsByUsername(String username);
	Optional<BaseAccount> findByUsername(String username);
	
}
