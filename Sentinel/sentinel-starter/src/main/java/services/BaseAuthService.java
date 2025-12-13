package services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import annotations.Logged;
import config.SecurityProperties;
import exceptions.AuthException;
import exceptions.BaseAuthError;
import lombok.RequiredArgsConstructor;
import model.entities.BaseSecurityToken;
import model.entities.account.BaseAccount;
import model.records.AccountRequest;
import model.records.RefreshRequest;
import repositories.SecurityTokenRepository;
import repositories.account.AccountRepository;

@Service
@RequiredArgsConstructor
public abstract class BaseAuthService implements AuthService {

	protected final SecurityProperties securityProperties;
	protected final AccountRepository accountRepository;
	protected final SecurityTokenRepository tokenRepository;
	protected final PasswordEncoder passwordEncoder;
	
	@Logged("Authentication")
	@Transactional
	public BaseSecurityToken authenticate(AccountRequest request) throws AuthException {
		verifyLoginRequest(request);
		BaseAccount account = findAccount(request);
		
		if (!passwordEncoder.matches(request.password(), account.getPassword())) {
			throw new AuthException(BaseAuthError.INVALID_CREDENTIALS, HttpStatus.FORBIDDEN);
		}
		
		Optional<BaseSecurityToken> token = tokenRepository.findByAccount(account);
		BaseSecurityToken result;

		if (token.isEmpty()) {
			result = generateNewToken(account);
		} else {
			BaseSecurityToken oldToken = token.get();
			if (oldToken.getAccessExpirationTime().isBefore(LocalDateTime.now())) {
				destroyToken(oldToken);
				BaseSecurityToken newToken = generateNewToken(account);
				result = newToken;
			} else {
				result = oldToken;
			}
		}
		
		return result;
	}
	
	@Logged("Registration")
	@Transactional
	public BaseSecurityToken register(AccountRequest request) throws AuthException {
		if (StringUtils.isBlank(request.username())) {
			throw new AuthException(BaseAuthError.MISSING_USERNAME, HttpStatus.BAD_REQUEST);
		}

		if (StringUtils.isBlank(request.password())) {
			throw new AuthException(BaseAuthError.MISSING_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		
		if (!StringUtils.isBlank(securityProperties.getAuth().getUsernameRestriction())) {
			if (!Pattern.matches(securityProperties.getAuth().getUsernameRestriction(), request.username())) {
				throw new AuthException(BaseAuthError.USERNAME_UNSUITABLE, HttpStatus.BAD_REQUEST);
			}
		}
		
		if (!StringUtils.isBlank(securityProperties.getAuth().getPasswordRestriction())) {
			if (!Pattern.matches(securityProperties.getAuth().getPasswordRestriction(), request.password())) {
				throw new AuthException(BaseAuthError.PASSWORD_UNSUITABLE, HttpStatus.BAD_REQUEST);
			}
		}
		
		Optional<BaseAuthError> accountAlreadyExists = checkAccount(request);
				
		if (accountAlreadyExists.isPresent()) {
			throw new AuthException(accountAlreadyExists.get(), HttpStatus.FORBIDDEN);
		}
		
		final BaseAccount account = registerAccount(request);
		final BaseSecurityToken token = generateNewToken(account);
		
		return token;
	}
	
	@Logged("Refresh Token")
	@Transactional
	public BaseSecurityToken refresh(RefreshRequest request) throws AuthException {
		if (request.refreshToken() == null) {
			throw new AuthException(BaseAuthError.MISSING_REFRESH_TOKEN, HttpStatus.FORBIDDEN);
		}
		
		Optional<BaseSecurityToken> token = tokenRepository.findByRefreshToken(request.refreshToken());
		
		BaseSecurityToken result = token.orElseThrow(() -> {
			throw new AuthException(BaseAuthError.MISSING_REFRESH_TOKEN, HttpStatus.FORBIDDEN);
		});
		
		if (result.getRefreshExpirationTime().isBefore(LocalDateTime.now())) {
			throw new AuthException(BaseAuthError.MISSING_REFRESH_TOKEN, HttpStatus.FORBIDDEN);
		}
		
		result = generateAccessToken(result);
		
		return result;
	}
	
	public BaseAccount getAuthenticatedAccount() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication instanceof AnonymousAuthenticationToken) {
			throw new AuthException(BaseAuthError.UNAUTHENTICATED, HttpStatus.FORBIDDEN);
		}

		try {
			return (BaseAccount) authentication.getPrincipal();
		} catch (ClassCastException exception) {
			throw new AuthException(BaseAuthError.MALFORMED_AUTH, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	protected abstract Optional<BaseAuthError> checkAccount(AccountRequest request);
	protected abstract BaseAccount registerAccount(AccountRequest request);
	protected abstract BaseAccount createAccount(AccountRequest request);
	protected abstract void verifyLoginRequest(AccountRequest request);
	protected abstract BaseAccount findAccount(AccountRequest request);
	
	protected BaseSecurityToken createToken(BaseAccount account) {
		return BaseSecurityToken.builder()
				.account(account)
				.build();
	}
	
	private BaseSecurityToken generateNewToken(BaseAccount account) {
		BaseSecurityToken token = createToken(account);
		token = generateAccessToken(token);
		token = generateRefreshToken(token);
		
		tokenRepository.save(token);
		
		return token;
	}
	
	private BaseSecurityToken generateAccessToken(BaseSecurityToken token) {
		token.setAccessToken(UUID.randomUUID());
		token.setAccessExpirationTime(LocalDateTime.now().plusHours(1));
		return token;
	}
	
	private BaseSecurityToken generateRefreshToken(BaseSecurityToken token) {
		token.setRefreshToken(UUID.randomUUID());
		token.setRefreshExpirationTime(LocalDateTime.now().plusHours(72));
		return token;
	}
	
	private void destroyToken(BaseSecurityToken token) {
		tokenRepository.delete(token);
	}
	
}
