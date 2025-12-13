package properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import config.AbstractTest;
import config.SecurityProperties;
import config.SecurityProperties.AuthProperties;
import config.SecurityProperties.UniquenessMode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@ActiveProfiles("test")
public class PropertiesTest extends AbstractTest {
	
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	
	@Test
	void defaultValues() {
		SecurityProperties properties = new SecurityProperties();
		
		Set<ConstraintViolation<SecurityProperties>> violations = validator.validate(properties);
		
		assertThat(violations).isEmpty();
		assertThat(properties.getAuth().getUsernameRestriction()).isEqualTo("");
		assertThat(properties.getAuth().getPasswordRestriction()).isEqualTo("");
		assertThat(properties.getAuth().getUniquenessMode()).isEqualTo(UniquenessMode.USERNAME);
		assertThat(properties.getAuth().isDefaultEndpointsEnabled()).isTrue();
	}
	
	@Test
	void invalidUsernameRestriction() {
		SecurityProperties properties = new SecurityProperties();
		properties.getAuth().setUsernameRestriction("[");
		
		Set<ConstraintViolation<AuthProperties>> violations = validator.validate(properties.getAuth());
		
		assertThat(violations).isNotEmpty();
	}
	
	@Test
	void invalidPasswordRestriction() {
		SecurityProperties properties = new SecurityProperties();
		properties.getAuth().setPasswordRestriction(")");
		
		Set<ConstraintViolation<AuthProperties>> violations = validator.validate(properties.getAuth());
		
		assertThat(violations).isNotEmpty();
	}
	
	@Test
	void invalidEnableDefautlEndpoint() {
		SecurityProperties properties = new SecurityProperties();
		properties.getAuth().setEnableDefaultEndpoints("maybe");
		
		Set<ConstraintViolation<AuthProperties>> violations = validator.validate(properties.getAuth());
		
		assertThat(violations).isNotEmpty();
	}

}
