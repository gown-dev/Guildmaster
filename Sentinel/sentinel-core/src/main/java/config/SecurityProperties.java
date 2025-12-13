package config;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@ConfigurationProperties(prefix = "sentinel")
public class SecurityProperties {
	
	private AuthProperties auth = new AuthProperties();

    @Data
    public static class AuthProperties {
    	private static final Set<String> VALID_HTTP_METHODS = Set.of("GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT",
    			"OPTIONS", "TRACE", "PATCH");
    	
        private String[] publicPaths = { "/auth/register", "/auth/login", "/auth/refresh" };
        private String[] allowedOrigins = { };
        private String[] allowedMethods = { "GET", "POST", "PUT", "DELETE", "OPTIONS" };
        private String[] allowedHeaders = { "*" };
        private String[] exposedHeaders = { "Authorization" };
        
        private String usernameRestriction = "";
        private String passwordRestriction = "";
        
        private UniquenessMode uniquenessMode = UniquenessMode.USERNAME;
        
        private String[] defaultRoles = { };
        
        @Getter(AccessLevel.NONE)
        private String enableDefaultEndpoints = "true";
        
        public boolean isDefaultEndpointsEnabled() {
        	return Boolean.valueOf(enableDefaultEndpoints);
        }
        
        @AssertTrue(message = "The values of sentinel.auth.allowedMethods must use a combination of : GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE or PATCH")
        public boolean areAllowedMethodsValid() {
        	if (allowedMethods == null || allowedMethods.length == 0) {
                return true;
            }

            for (String method : allowedMethods) {
                if (method == null || method.trim().isEmpty()) {
                    return false; 
                }
                
                if (!VALID_HTTP_METHODS.contains(method.trim().toUpperCase())) {
                    return false;
                }
            }
            return true;
        }
        
        @AssertTrue(message = "The value of sentinel.auth.usernameRestriction must be a valid regexp.")
        public boolean isUsernameRestrictionValid() {
            if (StringUtils.isBlank(usernameRestriction)) {
                return true; 
            }
            
            try {
                Pattern.compile(usernameRestriction);
                return true; 
            } catch (PatternSyntaxException e) {
                return false; 
            }
        }
        
        @AssertTrue(message = "The value of sentinel.auth.passwordRestriction must be a valid regexp.")
        public boolean isPasswordRestrictionValid() {
            if (StringUtils.isBlank(passwordRestriction)) {
                return true; 
            }
            
            try {
                Pattern.compile(passwordRestriction);
                return true; 
            } catch (PatternSyntaxException e) {
                return false; 
            }
        }
        
        @AssertTrue(message = "The value of sentinel.auth.enableDefaultEndpoints must be either \"true\" or \"false\".")
        public boolean isEnableDefaultEndpointsValid() {
        	if (StringUtils.isBlank(enableDefaultEndpoints)) {
        		return false;
        	}
        	
        	return "TRUE".equals(enableDefaultEndpoints.toUpperCase()) || "FALSE".equals(enableDefaultEndpoints.toUpperCase());
        }
    }
    
    public enum UniquenessMode {
        USERNAME,
        USERNAME_TAG
    }

}
