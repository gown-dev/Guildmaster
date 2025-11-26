package services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import model.entities.BaseAccount;
import model.entities.BaseSecurityToken;
import model.entities.Role;
import repositories.BaseAccountRepository;
import repositories.BaseSecurityTokenRepository;

@Service
@RequiredArgsConstructor
public class BaseAuthService implements AuthService {
	
	protected final SecurityProperties securityProperties;
	protected final BaseAccountRepository accountRepository;
	protected final BaseSecurityTokenRepository tokenRepository;
	protected final PasswordEncoder passwordEncoder;
	
	@Logged("Authentication")
	@Transactional
	public BaseSecurityToken authenticate(String username, String password) {
		BaseAccount account = accountRepository.findByUsername(username).orElseThrow(() -> {
			return new AuthException(BaseAuthError.INVALID_CREDENTIALS, HttpStatus.FORBIDDEN);
		});
		
		if (!passwordEncoder.matches(password, account.getPassword())) {
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
	
	private void destroyToken(BaseSecurityToken token) {
		tokenRepository.delete(token);
	}
	
	@Logged("Registration")
	@Transactional
	public BaseSecurityToken register(String username, String password) {
		if (securityProperties.getAuth().isUsernameRestricted()) {
			if (!Pattern.matches(securityProperties.getAuth().getUsernameRegexp(), username)) {
				throw new AuthException(BaseAuthError.USERNAME_UNSUITABLE, HttpStatus.BAD_REQUEST);
			}
		}
		
		if (securityProperties.getAuth().isPasswordRestricted()) {
			if (!Pattern.matches(securityProperties.getAuth().getPasswordRegexp(), password)) {
				throw new AuthException(BaseAuthError.PASSWORD_UNSUITABLE, HttpStatus.BAD_REQUEST);
			}
		}
				
		if (accountRepository.existsByUsername(username)) {
			throw new AuthException(BaseAuthError.USERNAME_TAKEN, HttpStatus.FORBIDDEN);
		}
		
		final BaseAccount account = registerAccount(username, password);
		final BaseSecurityToken token = generateNewToken(account);
		
		return token;
	}
	
	public BaseAccount registerAccount(String username, String password) {
		BaseAccount account = createAccount(username, password);
		accountRepository.save(account);
		
		return account;
	}
	
	protected BaseAccount createAccount(String username, String password) {
		return BaseAccount.builder()
				.username(username)
				.password(passwordEncoder.encode(password))
				.authorities(Arrays.asList(securityProperties.getAuth().getDefaultRoles())
						.stream()
						.map(Role::new)
						.collect(Collectors.toList()))
				.build();
	}
	
	private BaseSecurityToken generateNewToken(BaseAccount account) {
		BaseSecurityToken token = createToken(account);
		token = generateAccessToken(token);
		token = generateRefreshToken(token);
		
		tokenRepository.save(token);
		
		return token;
	}
	
	protected BaseSecurityToken createToken(BaseAccount account) {
		return BaseSecurityToken.builder()
				.account(account)
				.build();
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
	
	@Logged("Refresh Token")
	@Transactional
	public BaseSecurityToken refresh(UUID refreshToken) {
		Optional<BaseSecurityToken> token = tokenRepository.findByRefreshToken(refreshToken);
		
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
	
}
