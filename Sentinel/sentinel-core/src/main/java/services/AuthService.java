package services;

import java.util.UUID;

import model.entities.BaseSecurityToken;

public interface AuthService {

	public BaseSecurityToken authenticate(String username, String password);
	public BaseSecurityToken register(String username, String password);
	public BaseSecurityToken refresh(UUID refreshToken);
	
}
