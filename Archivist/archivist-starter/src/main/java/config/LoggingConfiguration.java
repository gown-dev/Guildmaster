package config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import aspects.LoggingAspect;

@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfiguration {
	
	@Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

}
