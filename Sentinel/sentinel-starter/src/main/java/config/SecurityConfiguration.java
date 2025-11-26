package config;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import exceptions.AuthExceptionHandler;
import filters.BearerTokenFilter;
import lombok.RequiredArgsConstructor;
import repositories.BaseAccountRepository;
import repositories.BaseSecurityTokenRepository;
import services.AuthService;
import services.BaseAuthService;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity(debug = false)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfiguration {
	
    private final SecurityProperties securityProperties;
	private final BearerTokenFilter bearerTokenFilter;
	
	@Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(false); 
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(1000);
        filter.setBeforeMessagePrefix("BEFORE REQUEST: [");
        filter.setAfterMessagePrefix("AFTER REQUEST: [");
        
        return filter;
    }
	
	@Bean
	@ConditionalOnMissingBean(SecurityFilterChain.class)
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.authorizeHttpRequests((request) -> {
			request.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
			request.requestMatchers(securityProperties.getAuth().getPublicPaths()).permitAll();
			request.anyRequest().authenticated();
		})
		.csrf(AbstractHttpConfigurer::disable)
		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
		.addFilterAfter(bearerTokenFilter, BasicAuthenticationFilter.class)
		.build();
	}
	
	@Bean
	@ConditionalOnMissingBean(CorsConfigurationSource.class)
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList(securityProperties.getAuth().getAllowedOrigins()));
		configuration.setAllowedMethods(Arrays.asList(securityProperties.getAuth().getAllowedMethods()));
		configuration.setAllowedHeaders(Arrays.asList(securityProperties.getAuth().getAllowedHeaders()));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(Arrays.asList(securityProperties.getAuth().getExposedHeaders()));
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
	@Bean
	@ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
    @ConditionalOnMissingBean(AuthExceptionHandler.class) 
    public AuthExceptionHandler authExceptionHandler() {
        return new AuthExceptionHandler();
    }
	
	@Bean
    @ConditionalOnProperty(prefix = "sentinel.auth.token", name = "enable-bearer-filter", matchIfMissing = true) 
    public BearerTokenFilter bearerTokenFilter(BaseSecurityTokenRepository repository) {
        return new BearerTokenFilter(repository);
    }
	
	@Bean
	@ConditionalOnMissingBean(AuthService.class)
	public AuthService baseAuthService(BaseAccountRepository accountRepository, BaseSecurityTokenRepository tokenRepository) {
		return new BaseAuthService(securityProperties, accountRepository, tokenRepository, this.passwordEncoder());
	}
	
}
