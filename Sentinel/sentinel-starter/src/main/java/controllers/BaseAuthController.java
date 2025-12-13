package controllers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import model.entities.BaseSecurityToken;
import model.records.AccountRequest;
import model.records.RefreshRequest;
import services.AuthService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@ConditionalOnProperty(prefix = "sentinel.auth", name = "enable-default-endpoints", havingValue = "true", matchIfMissing = true) 
public class BaseAuthController {
	
	private final AuthService authService;

	@PostMapping("/register")
    public ResponseEntity<BaseSecurityToken> register(@RequestBody AccountRequest request) {
		BaseSecurityToken response = authService.register(request);
        return ResponseEntity.ok(response);
    }
	
	@PostMapping("/login")
    public ResponseEntity<BaseSecurityToken> login(@RequestBody AccountRequest request) {
		BaseSecurityToken response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
	
	@GetMapping("/refresh")
    public ResponseEntity<BaseSecurityToken> refresh(@RequestBody RefreshRequest request) {
		BaseSecurityToken response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }
	
}