package config;

import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "bard")
public class InternationalisationProperties {

	private i18nProperties i18n = new i18nProperties(); 

    @Data
    public static class i18nProperties {
        private String defaultLocale = Locale.ENGLISH.getLanguage();
        private String messagesDirectory = "classpath:i18n/messages";
        private String encoding = "UTF-8";
    }
    
}
