package services;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import config.SecurityProperties;
import exceptions.AuthException;
import exceptions.BaseAuthError;
import model.entities.account.BaseAccount;
import model.records.AccountRequest;
import repositories.SecurityTokenRepository;
import repositories.account.AccountRepository;

@Service
@ConditionalOnProperty(prefix = "sentinel.auth", name = "uniqueness-mode", havingValue = "username-tag") 
public class UsernameTagAuthService extends BaseAuthService {
		
	public UsernameTagAuthService(SecurityProperties securityProperties,
			AccountRepository accountRepository,
			SecurityTokenRepository tokenRepository,
			PasswordEncoder passwordEncoder) {
		super(securityProperties, accountRepository, tokenRepository, passwordEncoder);
	}
	
	@Override
	protected void verifyLoginRequest(AccountRequest request) {
		if (StringUtils.isBlank(request.username())) {
			throw new AuthException(BaseAuthError.MISSING_USERNAME, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isBlank(request.tag())) {
			throw new AuthException(BaseAuthError.MISSING_TAG, HttpStatus.BAD_REQUEST);
		}
	
		if (StringUtils.isBlank(request.password())) {
			throw new AuthException(BaseAuthError.MISSING_PASSWORD, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	protected BaseAccount findAccount(AccountRequest request) {
		return accountRepository.findByUsernameAndTag(request.username(), request.tag()).orElseThrow(() -> {
			return new AuthException(BaseAuthError.INVALID_CREDENTIALS, HttpStatus.FORBIDDEN);
		});
	}
	
	@Override
	protected Optional<BaseAuthError> checkAccount(AccountRequest request) {
		if (accountRepository.existsByUsernameAndTag(request.username(), request.tag())) {
			return Optional.of(BaseAuthError.USERNAME_AND_TAG_TAKEN);
		} else {
			return Optional.empty();
		}
	}

	@Override
	protected BaseAccount registerAccount(AccountRequest request) {
		BaseAccount account = createAccount(request);
		accountRepository.save(account);
		
		return account;
	}

	@Override
	protected BaseAccount createAccount(AccountRequest request) {
		return BaseAccount.builder()
			.username(request.username())
			.tag(request.tag())
			.password(passwordEncoder.encode(request.password()))
			.authorities(Arrays.asList(securityProperties.getAuth().getDefaultRoles())
				.stream()
				.collect(Collectors.toList()))
			.build();
	}

}
