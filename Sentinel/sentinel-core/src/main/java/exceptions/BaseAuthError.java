package exceptions;

import lombok.Getter;

@Getter
public enum BaseAuthError implements AuthError {

	UNAUTHENTICATED("ERR_T001", "Unauthenticated", "Unable to read the caller account object."),
	MALFORMED_AUTH("ERR_T002", "Malformed authentication", "The authentication object was malformed, and the application was unable to read it."),
	
	USERNAME_UNSUITABLE("ERR_F001", "Unsuitable username", "Username did not comply with the restrictions for registration."),
	PASSWORD_UNSUITABLE("ERR_F002", "Unsuitable password", "Password did not comply with the restrictions for registration."),
	USERNAME_TAKEN("ERR_F003", "Username already in use", "Username was already used for another Account."),
	MISSING_REFRESH_TOKEN("ERR_F004", "Missing or invalid refresh token", "Refresh token was found to be null or expired."),
	MALFORMED_REFRESH_TOKEN("ERR_F005", "Malformed refresh token", "Refresh token was not coherent with the expected format."),
	INVALID_CREDENTIALS("ERR_F006", "Invalid credentials", "Unable to authenticate the user with the provided credentials.");

	public String code;
	public String description;
	public String message;
	
	BaseAuthError(String code, String description, String message) {
		this.code = code;
		this.description = description;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "";
	}
	
}
