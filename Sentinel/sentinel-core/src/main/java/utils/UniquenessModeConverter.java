package utils;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import config.SecurityProperties.UniquenessMode;

@Component
public class UniquenessModeConverter implements Converter<String, UniquenessMode> {

    @Override
    public UniquenessMode convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        try {
            return UniquenessMode.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = List.of(UniquenessMode.values()).stream()
            		.map(value -> String.valueOf(value))
            		.collect(Collectors.joining(", "));
            
            throw new IllegalArgumentException(
                "Invalid value for configuration variable 'sentinel.auth.uniqueness-mode' : '" + source + 
                "'. Accepted values are : " + validValues + "."
            );
        }
    }
}