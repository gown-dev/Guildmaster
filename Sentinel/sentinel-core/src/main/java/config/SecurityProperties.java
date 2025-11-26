package config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "sentinel")
public class SecurityProperties {
	
	private AuthProperties auth = new AuthProperties(); 

    @Data
    public static class AuthProperties {
        private String[] publicPaths = { "/auth/register", "/auth/login", "/auth/refresh" };
        private String[] allowedOrigins = { };
        private String[] allowedMethods = { "GET", "POST", "PUT", "DELETE", "OPTIONS" };
        private String[] allowedHeaders = { "*" };
        private String[] exposedHeaders = { "Authorization" };
        
        private boolean usernameRestricted = false;
        private String usernameRegexp = "";
        
        private boolean passwordRestricted = false;
        private String passwordRegexp = "";
        
        private String[] defaultRoles = { };
        
        private boolean enableDefaultEndpoints = true;
    }

}
