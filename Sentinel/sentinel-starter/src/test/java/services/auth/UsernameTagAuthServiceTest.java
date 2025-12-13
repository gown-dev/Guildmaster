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
import services.UsernameTagAuthService;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
@TestPropertySource(properties = { "sentinel.auth.uniqueness-mode=username-tag" })
public class UsernameTagAuthServiceTest extends AuthServiceTest {
	
    @Autowired
    protected UsernameTagAuthService authService;
    
	@Override
	protected UsernameTagAuthService getAuthService() {
		return authService;
	}
	
    @Test
    void authenticateFailureTagTest() {
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	createAccount(username, tag, password);
    	
    	AccountRequest request = AccountRequest.builder()
    		.username(username)
    		.tag("NA")
    		.password(password)
    		.build();
    	    	
    	assertAuthException(BaseAuthError.INVALID_CREDENTIALS, () -> authService.authenticate(request));
    }
    
    @Test
    void registerFailureTagMissingTest() {
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	AccountRequest request = AccountRequest.builder()
    			.username(username)
        		.password(password)
        		.build();
    	
    	assertAuthException(BaseAuthError.MISSING_TAG, () -> authService.authenticate(request));
    }
    
    @Test
    void registerFailureUsernameAndTagTaken() {
    	String username = "Gown";
    	String tag = "EUW";
    	String password = "p455w0rd";
    	
    	createAccount(username, tag, password);
    	
    	AccountRequest request = AccountRequest.builder()
    			.username(username)
        		.tag(tag)
        		.password("5tr0ng_p455w0rd")
        		.build();
    	
    	assertAuthException(BaseAuthError.USERNAME_AND_TAG_TAKEN, () -> authService.register(request));
    }

}
