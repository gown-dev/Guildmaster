package config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import services.NoTranslationService;
import services.TranslationService;

@Configuration
public class DependencyConfiguration {
	
	@Bean
	@ConditionalOnMissingBean(TranslationService.class)
	public TranslationService defaultTranslationService() {
	    return new NoTranslationService();
	}

}
