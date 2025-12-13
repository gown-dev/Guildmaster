package config;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import controllers.BaseAuthController;
import exceptions.AuthExceptionHandler;
import filters.BearerTokenFilter;
import lombok.RequiredArgsConstructor;
import repositories.SecurityTokenRepository;
import repositories.account.AccountRepository;
import services.AuthService;
import services.UsernameAuthService;
import services.UsernameTagAuthService;
import utils.UniquenessModeConverter;

@AutoConfiguration
@RequiredArgsConstructor
@EnableWebSecurity(debug = false)
@EntityScan(basePackages = "model.entities")
@EnableJpaRepositories(basePackages = "repositories")
@EnableConfigurationProperties(SecurityProperties.class)
public class Autoconfiguration {
    
    private final SecurityProperties securityProperties;
    
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenFilter bearerTokenFilter) throws Exception {
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
    public BearerTokenFilter bearerTokenFilter(SecurityTokenRepository repository) {
        return new BearerTokenFilter(repository);
    }
    
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService(AccountRepository accountRepository) {
        return username -> accountRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
    
    @Bean
    @ConditionalOnMissingBean(AuthService.class)
    @ConditionalOnProperty(prefix = "sentinel.auth", name = "uniqueness-mode", havingValue = "username", matchIfMissing = true)
    public AuthService usernameAuthService(AccountRepository accountRepository, SecurityTokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        return new UsernameAuthService(securityProperties, accountRepository, tokenRepository, passwordEncoder);
    }
    
    @Bean
    @ConditionalOnMissingBean(AuthService.class)
    @ConditionalOnProperty(prefix = "sentinel.auth", name = "uniqueness-mode", havingValue = "username-tag")
    public AuthService usernameTagAuthService(AccountRepository accountRepository, SecurityTokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        return new UsernameTagAuthService(securityProperties, accountRepository, tokenRepository, passwordEncoder);
    }
    
    @Bean
    public UniquenessModeConverter uniquenessModeConverter() {
        return new UniquenessModeConverter();
    }
    
    @Bean
    @ConditionalOnMissingBean(BaseAuthController.class)
    @ConditionalOnProperty(prefix = "sentinel.auth", name = "enable-default-endpoints", havingValue = "true", matchIfMissing = true)
    public BaseAuthController baseAuthController(AuthService authService) {
        return new BaseAuthController(authService);
    }
}