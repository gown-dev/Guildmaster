package controllers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import model.dto.LoginRequest;
import model.dto.RefreshRequest;
import model.dto.RegisterRequest;
import model.entities.BaseSecurityToken;
import services.BaseAuthService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@ConditionalOnProperty(prefix = "sentinel.auth", name = "enable-default-endpoints", havingValue = "true", matchIfMissing = true) 
public class BaseAuthController {
	
	private final BaseAuthService authService;

	@PostMapping("/register")
    public ResponseEntity<BaseSecurityToken> register(@RequestBody RegisterRequest request) {
		BaseSecurityToken response = authService.register(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
	
	@PostMapping("/login")
    public ResponseEntity<BaseSecurityToken> login(@RequestBody LoginRequest request) {
		BaseSecurityToken response = authService.authenticate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
	
	@GetMapping("/refresh")
    public ResponseEntity<BaseSecurityToken> refresh(@RequestBody RefreshRequest request) {
		BaseSecurityToken response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
	
}