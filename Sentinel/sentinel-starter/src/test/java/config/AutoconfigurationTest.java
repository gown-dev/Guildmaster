package config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ActiveProfiles;

import controllers.BaseAuthController;
import repositories.SecurityTokenRepository;
import repositories.account.AccountRepository;
import services.AuthService;
import services.UsernameAuthService;
import services.UsernameTagAuthService;
import utils.UniquenessModeConverter;

@ActiveProfiles("test")
public class AutoconfigurationTest extends AbstractTest {
	
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(
	            DataSourceAutoConfiguration.class,
	            HibernateJpaAutoConfiguration.class))
			.withUserConfiguration(Autoconfiguration.class)
	        .withPropertyValues(
	            "spring.datasource.url=jdbc:h2:mem:testdb",
	            "spring.datasource.driver-class-name=org.h2.Driver",
	            "spring.jpa.hibernate.ddl-auto=create-drop");
	
	@Test
	public void defaultAutoconfiguration() {
		contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuthService.class);
            assertThat(context).hasSingleBean(SecurityTokenRepository.class);
            assertThat(context).hasSingleBean(SecurityProperties.class);
            assertThat(context).hasSingleBean(AccountRepository.class);
            assertThat(context).hasSingleBean(UniquenessModeConverter.class);
        });
	}
	
	@Test
	public void usernameAutoconfiguration() {
		contextRunner.withPropertyValues("sentinel.auth.uniqueness-mode = username")
        .run(context -> {            
            assertThat(context).hasSingleBean(UsernameAuthService.class);
            assertThat(context).doesNotHaveBean(UsernameTagAuthService.class);
        });
	}
	
	@Test
	public void usernameTagAutoconfiguration() {
		contextRunner.withPropertyValues("sentinel.auth.uniqueness-mode = username-tag")
        .run(context -> {
            assertThat(context).hasSingleBean(UsernameTagAuthService.class);
            assertThat(context).doesNotHaveBean(UsernameAuthService.class);
        });
	}
	
	@Test
	public void defaultEndpointsEnabledAutoconfiguration() {
		contextRunner.withPropertyValues("sentinel.auth.enable-default-endpoints = true")
        .run(context -> {
            assertThat(context).hasSingleBean(BaseAuthController.class);
        });
	}
	
	@Test
	public void defaultEndpointsDisabledAutoconfiguration() {
		contextRunner.withPropertyValues("sentinel.auth.enable-default-endpoints = false")
        .run(context -> {
            assertThat(context).doesNotHaveBean(BaseAuthController.class);
        });
	}
    
}