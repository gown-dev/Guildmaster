package services.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import config.Autoconfiguration;
import exceptions.BaseAuthError;
import model.records.AccountRequest;
import services.UsernameAuthService;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
@TestPropertySource(properties = { "sentinel.auth.uniqueness-mode=username" })
public class UsernameAuthServiceTest extends AuthServiceTest {
	
    @Autowired
    protected UsernameAuthService authService;
    
	@Override
	protected UsernameAuthService getAuthService() {
		return authService;
	}
    
    @Test
    void authenticateSuccessTagTest() {
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	createAccount(username, tag, password);
    	
    	AccountRequest request = AccountRequest.builder()
    		.username(username)
    		.tag("NA")
    		.password(password)
    		.build();
    	    	    	
    	assertValidToken(() -> authService.authenticate(request));
    }
    
    @Test
    void registerSuccessTagMissingTest() {
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	AccountRequest request = AccountRequest.builder()
    			.username(username)
        		.password(password)
        		.build();
    	
    	assertValidToken(() -> authService.register(request));
    }
    
    @Test
    void registerFailureUsernameTaken() {
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	createAccount(username, tag, password);
    	
    	AccountRequest request = AccountRequest.builder()
    			.username(username)
        		.tag("NA")
        		.password("5tr0ng_p455w0rd")
        		.build();
    	
    	assertAuthException(BaseAuthError.USERNAME_TAKEN, () -> getAuthService().register(request));
    }

}
