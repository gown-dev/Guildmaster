package services;

public interface TranslationService {

	String translate(String key, String lang);
    String translateDefault(String key);
    
}
