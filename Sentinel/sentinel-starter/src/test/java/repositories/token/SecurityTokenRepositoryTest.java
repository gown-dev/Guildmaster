package repositories.token;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import model.entities.BaseSecurityToken;
import model.entities.account.BaseAccount;
import repositories.SecurityTokenRepository;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
class SecurityTokenRepositoryTest extends AbstractTest {
    
    @Autowired
    private SecurityTokenRepository securityRepository;
    
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
    
    private BaseSecurityToken createToken(UUID accessToken, UUID refreshToken) {
    	BaseAccount account = createAccount("Gown", "EUW");
    	
    	BaseSecurityToken token = BaseSecurityToken.builder()
    		.account(account)
    		.accessToken(accessToken)
    		.accessExpirationTime(LocalDateTime.now().plusDays(1))
    		.refreshToken(refreshToken)
    		.refreshExpirationTime(LocalDateTime.now().plusDays(3))
			.build();
    	
    	return entityManager.persistAndFlush(token);
    }
    
    @BeforeEach
    void setUp() {
    	securityRepository.deleteAll();
    }
    
    @AfterEach
    void cleanUp() {
    	securityRepository.deleteAll();
    }
    
    @Test
    void saveToken() {
    	BaseSecurityToken token = createToken(
    			UUID.fromString("00000000-0000-0000-0000-000000000000"), 
    			UUID.fromString("11111111-1111-1111-1111-111111111111"));
        
        assertThat(token.getId()).isNotNull();
    }
    
    @Test
    void deleteToken() {
    	BaseSecurityToken token = createToken(
    			UUID.fromString("00000000-0000-0000-0000-000000000000"), 
    			UUID.fromString("11111111-1111-1111-1111-111111111111"));
    	
    	assertThat(securityRepository.count()).isEqualTo(1);
    	
    	securityRepository.delete(token);
    	
    	assertThat(securityRepository.count()).isEqualTo(0);
    }
    
    @Test
    void findTokenByAccessToken() {
    	UUID accessToken1 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    	UUID refreshToken1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    	BaseSecurityToken token1 = createToken(accessToken1, refreshToken1);
    	
    	UUID accessToken2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    	UUID refreshToken2 = UUID.fromString("33333333-3333-3333-3333-333333333333");
    	BaseSecurityToken token2 = createToken(accessToken2, refreshToken2);
    	
    	Optional<BaseSecurityToken> result = securityRepository.findByAccessToken(accessToken1);
    	
    	assertThat(result).isNotEmpty();
    	assertThat(result.get()).isEqualTo(token1);
    	assertThat(result.get()).isNotEqualTo(token2);
    }
    
    @Test
    void findTokenByRefreshToken() {
    	UUID accessToken1 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    	UUID refreshToken1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    	BaseSecurityToken token1 = createToken(accessToken1, refreshToken1);
    	
    	UUID accessToken2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    	UUID refreshToken2 = UUID.fromString("33333333-3333-3333-3333-333333333333");
    	BaseSecurityToken token2 = createToken(accessToken2, refreshToken2);
    	
    	Optional<BaseSecurityToken> result = securityRepository.findByRefreshToken(refreshToken1);
    	
    	assertThat(result).isNotEmpty();
    	assertThat(result.get()).isEqualTo(token1);
    	assertThat(result.get()).isNotEqualTo(token2);
    }
    
    @Test
    void findTokenByAccount() {
    	UUID accessToken1 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    	UUID refreshToken1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    	BaseSecurityToken token1 = createToken(accessToken1, refreshToken1);
    	
    	UUID accessToken2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    	UUID refreshToken2 = UUID.fromString("33333333-3333-3333-3333-333333333333");
    	BaseSecurityToken token2 = createToken(accessToken2, refreshToken2);
    	
    	Optional<BaseSecurityToken> result = securityRepository.findByAccount(token1.getAccount());
    	
    	assertThat(result).isNotEmpty();
    	assertThat(result.get()).isEqualTo(token1);
    	assertThat(result.get()).isNotEqualTo(token2);
    }
    
}