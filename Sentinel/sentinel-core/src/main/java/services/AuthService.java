package services;

import model.entities.BaseSecurityToken;
import model.records.AccountRequest;
import model.records.RefreshRequest;

public interface AuthService {

	public BaseSecurityToken authenticate(AccountRequest request);
	public BaseSecurityToken register(AccountRequest request);
	public BaseSecurityToken refresh(RefreshRequest request);
	
}
