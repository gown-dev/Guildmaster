package controllers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import config.SecurityProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/supervision")
@ConditionalOnBean(SecurityProperties.class) 
public class SecuredLoggingController {
	
}