package services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BaseTranslationService implements TranslationService {

	private final Locale defaultLocale;
	private final MessageSource messageSource;
	private final List<String> missingKeys = new ArrayList<>();
	
	private Locale resolveLocale(String lang) {
        if (lang != null && !lang.isEmpty()) {
            return Locale.forLanguageTag(lang);
        }
        return defaultLocale;
    }
	
	@Override
	public String translate(String key, String lang) {
		Locale locale = resolveLocale(lang);
        try {
            return messageSource.getMessage(key, null, key, locale);
        } catch (NoSuchMessageException e) {
        	log.error("Missing translation for key '{}' in locale {}.", key, locale);
        	if (!missingKeys.contains(key)) {
        		missingKeys.add(key);
        		log.info("Missing key '{}' was added to the missing keys list.", key);
        	}
            return key; 
        }
	}

	@Override
	public String translateDefault(String key) {
		return messageSource.getMessage(key, null, key, defaultLocale);
	}

}
