package repositories.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import config.AbstractTest;
import config.Autoconfiguration;
import model.entities.account.BaseAccount;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
class AccountRepositoryTest extends AbstractTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private BaseAccount createAccount(String username, String tag) {
    	BaseAccount account = BaseAccount.builder()
			.username(username)
			.tag(tag)
			.password("p455w0rd")
			.active(true)
			.expired(false)
			.locked(false)
			.expiredCredentials(false)
			.authorities(List.of("ADMIN", "USER"))
			.build();
    	
    	return entityManager.persistAndFlush(account);
    }
    
    @BeforeEach
    void setUp() {
    	accountRepository.deleteAll();
    }
    
    @AfterEach
    void cleanUp() {
    	accountRepository.deleteAll();
    }
    
    @Test
    void saveAccount() {
    	BaseAccount account = createAccount("Gown", "EUW");
        
        assertThat(account.getId()).isNotNull();
    }
    
    @Test
    void deleteAccount() {
    	BaseAccount account = createAccount("Gown", "EUW");
    	
    	assertThat(accountRepository.count()).isEqualTo(1);
    	
    	accountRepository.delete(account);
    	
    	assertThat(accountRepository.count()).isEqualTo(0);
    }
    
    @Test
    void findAccountByUsername() {
    	BaseAccount account1 = createAccount("Gown", "EUW");
    	BaseAccount account2 = createAccount("Nwog", "EUW");
    	
    	Optional<BaseAccount> result = accountRepository.findByUsername("Gown");
    	
    	assertThat(result).isNotEmpty();
    	assertThat(result.get()).isEqualTo(account1);
    	assertThat(result.get()).isNotEqualTo(account2);
    }
    
    @Test
    void findAccountByUsernameAndTag() {
    	BaseAccount account1 = createAccount("Gown", "EUW");
    	BaseAccount account2 = createAccount("Gown", "NA");
    	
    	Optional<BaseAccount> result = accountRepository.findByUsernameAndTag("Gown", "EUW");
    	
    	assertThat(result).isNotEmpty();
    	assertThat(result.get()).isEqualTo(account1);
    	assertThat(result.get()).isNotEqualTo(account2);
    }
    
    @Test
    void existsByUsername() {
    	createAccount("Gown", "EUW");
    	
    	assertThat(accountRepository.existsByUsername("Gown")).isTrue();
    	assertThat(accountRepository.existsByUsername("GOWN")).isFalse();
    	assertThat(accountRepository.existsByUsername("Nwog")).isFalse();
    }
    
    @Test
    void existsByUsernameAndTag() {
    	createAccount("Gown", "EUW");
    	
    	assertThat(accountRepository.existsByUsernameAndTag("Gown", "EUW")).isTrue();
    	assertThat(accountRepository.existsByUsernameAndTag("Gown", "Euw")).isFalse();
    	assertThat(accountRepository.existsByUsernameAndTag("GOWN", "EUW")).isFalse();
    	assertThat(accountRepository.existsByUsernameAndTag("Gown", "NA")).isFalse();
    	assertThat(accountRepository.existsByUsernameAndTag("Nwog", "EUW")).isFalse();
    }
    
    @Test
    void findDuplicatedUsernames() {
    	createAccount("Gown", "EUW");
    	createAccount("Gown", "NA");
    	createAccount("Nwog", "EUW");
    	createAccount("Wogn", "NA");
    	
    	assertThat(accountRepository.findDuplicateUsernames()).isEqualTo(List.of("Gown"));
    }
    
}