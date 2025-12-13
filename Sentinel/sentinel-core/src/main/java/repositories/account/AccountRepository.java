package repositories.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import model.entities.account.BaseAccount;

public interface AccountRepository extends JpaRepository<BaseAccount, UUID>, JpaSpecificationExecutor<BaseAccount> {

	Optional<BaseAccount> findByUsername(String username);
	boolean existsByUsernameAndPassword(String username, String password);
	boolean existsByUsername(String username);
	
	Optional<BaseAccount> findByUsernameAndTag(String username, String tag);
	boolean existsByUsernameAndTagAndPassword(String username, String tag, String password);
	boolean existsByUsernameAndTag(String username, String tag);
	
	@Query(value = """
           SELECT a.username
           FROM BaseAccount a
           GROUP BY a.username
           HAVING COUNT(a.username) > 1
           """)
    List<String> findDuplicateUsernames();

}
