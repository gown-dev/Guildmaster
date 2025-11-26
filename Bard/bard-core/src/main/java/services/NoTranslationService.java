package services;

import org.springframework.stereotype.Service;

@Service
public final class NoTranslationService implements TranslationService {

	@Override
	public String translate(String key, String lang) {
		return key;
	}

	@Override
	public String translateDefault(String key) {
		return key;
	}

}
