package services.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import config.AbstractTest;
import config.Autoconfiguration;
import config.SecurityProperties;
import exceptions.AuthException;
import exceptions.BaseAuthError;
import model.entities.BaseSecurityToken;
import model.entities.account.BaseAccount;
import model.records.AccountRequest;
import model.records.RefreshRequest;
import repositories.SecurityTokenRepository;
import repositories.account.AccountRepository;
import services.AuthService;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
public abstract class AuthServiceTest extends AbstractTest {
    
	@Autowired
	protected SecurityProperties securityProperties;
	
    @Autowired
    protected TestEntityManager entityManager;
    
    @Autowired
    protected AccountRepository accountRepository;
    
    @Autowired
    protected SecurityTokenRepository tokenRepository;
    
    @Autowired
    protected PasswordEncoder passwordEncoder;
    
    protected abstract AuthService getAuthService();
    
    protected BaseAccount createAccount(String username, String tag, String password) {
    	BaseAccount account = BaseAccount.builder()
			.username(username)
			.tag(tag)
			.password(passwordEncoder.encode(password))
			.active(true)
			.expired(false)
			.locked(false)
			.expiredCredentials(false)
			.authorities(List.of("ADMIN", "USER"))
			.build();
    	
    	return entityManager.persistAndFlush(account);
    }

    protected BaseSecurityToken createToken(BaseAccount account, UUID accessToken, UUID refreshToken) {    	
    	BaseSecurityToken token = BaseSecurityToken.builder()
    		.account(account)
    		.accessToken(accessToken)
    		.accessExpirationTime(LocalDateTime.now().plusDays(1))
    		.refreshToken(refreshToken)
    		.refreshExpirationTime(LocalDateTime.now().plusDays(3))
			.build();
    	
    	return entityManager.persistAndFlush(token);
    }
    
    protected void assertValidToken(ThrowingSupplier<BaseSecurityToken> tokenSupplier) {
    	BaseSecurityToken token = assertDoesNotThrow(tokenSupplier);
    	assertNotNull(token.getAccessToken());
    	assertTrue(token.getAccessExpirationTime().isAfter(LocalDateTime.now()));
    	assertNotNull(token.getRefreshToken());
    	assertTrue(token.getRefreshExpirationTime().isAfter(LocalDateTime.now()));
    }
    
    protected void assertAuthException(BaseAuthError expectedError, Executable call) {
    	AuthException exception = assertThrows(AuthException.class, call);
    	assertTrue(expectedError.code.equals(exception.getError().getCode()));
    	assertTrue(expectedError.description.equals(exception.getError().getDescription()));
    	assertTrue(expectedError.message.equals(exception.getError().getMessage()));
    }
    
    @BeforeEach
    void setUp() {
    	tokenRepository.deleteAll();
    	accountRepository.deleteAll();
    }
    
    @AfterEach
    void cleanUp() {
    	tokenRepository.deleteAll();
    	accountRepository.deleteAll();
    }
    
    @Test
    void authenticateSuccessTest() {
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	createAccount(username, tag, password);
    	
    	assertTrue(accountRepository.count() == 1);
    	
    	AccountRequest request = AccountRequest.builder()
    		.username(username)
    		.tag(tag)
    		.password(password)
    		.build();
    	
    	assertValidToken(() -> getAuthService().authenticate(request));
    }
    
    @Test
    void authenticateFailureUsernameTest() {
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	createAccount(username, tag, password);
    	
    	AccountRequest request = AccountRequest.builder()
    		.username("Nwog")
    		.tag(tag)
    		.password(password)
    		.build();
    	    
    	assertAuthException(BaseAuthError.INVALID_CREDENTIALS, () -> getAuthService().authenticate(request));
    }
    
    @Test
    void registerSuccessTest() {
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	AccountRequest request = AccountRequest.builder()
        		.username(username)
        		.tag(tag)
        		.password(password)
        		.build();
    	
    	assertValidToken(() -> getAuthService().register(request));
    }
    
    @Test
    void registerFailureUsernameMissingTest() {
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	createAccount(username, tag, password);
    	
    	AccountRequest request = AccountRequest.builder()
        		.tag("NA")
        		.password("5tr0ng_p455w0rd")
        		.build();
    	
    	assertAuthException(BaseAuthError.MISSING_USERNAME, () -> getAuthService().register(request));
    }
    
    @Test
    void registerFailurePasswordMissingTest() {
    	String username = "Gown";
    	String tag = "EUW";
    	
    	AccountRequest request = AccountRequest.builder()
    			.username(username)
        		.tag(tag)
        		.build();
    	
    	assertAuthException(BaseAuthError.MISSING_PASSWORD, () -> getAuthService().register(request));
    }
    
    @Test
    @DirtiesContext
    void registerFailureUsernameUnsuitableTest() {
    	securityProperties.getAuth().setUsernameRestriction("^[A-Z]+$");
    	
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	AccountRequest request = AccountRequest.builder()
    			.username(username)
        		.tag(tag)
        		.password(password)
        		.build();
    	
    	assertAuthException(BaseAuthError.USERNAME_UNSUITABLE, () -> getAuthService().register(request));
    }
    
    @Test
    @DirtiesContext
    void registerSuccessUsernameSuitableTest() {
    	securityProperties.getAuth().setUsernameRestriction("^[A-Z]+$");
    	
    	String username = "GOWN";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	AccountRequest request = AccountRequest.builder()
    			.username(username)
        		.tag(tag)
        		.password(password)
        		.build();
    	
    	assertValidToken(() -> getAuthService().register(request));
    }
    
    @Test
    @DirtiesContext
    void registerFailurePasswordUnsuitableTest() {
    	/* Minimum eight characters, at least one uppercase letter, one lowercase letter and one number. */
    	securityProperties.getAuth().setPasswordRestriction("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");
    	
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "password";
    	
    	AccountRequest request = AccountRequest.builder()
    			.username(username)
        		.tag(tag)
        		.password(password)
        		.build();
    	
    	assertAuthException(BaseAuthError.PASSWORD_UNSUITABLE, () -> getAuthService().register(request));
    }
    
    @Test
    @DirtiesContext
    void registerSuccessPasswordSuitableTest() {
    	/* Minimum eight characters, at least one uppercase letter, one lowercase letter and one number. */
    	securityProperties.getAuth().setPasswordRestriction("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");
    	
    	String username = "GOWN";
    	String tag = "EUW";
    	String password = "P455w0rd";
    	
    	AccountRequest request = AccountRequest.builder()
    			.username(username)
        		.tag(tag)
        		.password(password)
        		.build();
    	
    	assertValidToken(() -> getAuthService().register(request));
    }
    
    @Test
    void refreshSuccessTest() {
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	BaseAccount account = createAccount(username, tag, password);
    	createToken(account, 
    			UUID.fromString("00000000-0000-0000-0000-000000000000"),
    			UUID.fromString("11111111-1111-1111-1111-111111111111"));
    	
    	assertTrue(accountRepository.count() == 1);
    	
    	RefreshRequest request = RefreshRequest.builder()
    		.refreshToken(UUID.fromString("11111111-1111-1111-1111-111111111111"))
    		.build();
    	
    	assertValidToken(() -> getAuthService().refresh(request));
    }
	
}
