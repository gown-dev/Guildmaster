package config;

import java.util.Locale;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import lombok.RequiredArgsConstructor;
import services.BaseTranslationService;
import services.TranslationService;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(InternationalisationProperties.class)
public class InternationalisationConfiguration {
	
	private final InternationalisationProperties properties;

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename(buildBasename(properties.getI18n().getMessagesDirectory())); 
        messageSource.setDefaultEncoding(properties.getI18n().getEncoding());
        messageSource.setCacheSeconds(1);
        
        return messageSource;
    }
    
    private String buildBasename(String basename) {
        if (basename == null || basename.isEmpty()) {
            return "classpath:i18n/messages";
        }
        
        if (basename.startsWith("classpath:") || basename.startsWith("file:")) {
            return basename;
        }
        
        return "classpath:" + basename;
    }

    @Bean
    @ConditionalOnMissingBean(TranslationService.class)
    public TranslationService baseTranslationService(MessageSource messageSource) {
    	Locale locale;
        try {
        	locale = Locale.forLanguageTag(properties.getI18n().getDefaultLocale());
        	
        	if (locale == null) {
            	locale = Locale.ENGLISH;
            }
        } catch (Exception e) {
        	locale = Locale.ENGLISH;
        }

        return new BaseTranslationService(locale, messageSource);
    }
    
}
