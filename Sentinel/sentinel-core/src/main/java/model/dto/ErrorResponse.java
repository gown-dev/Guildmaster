package model.dto;

import exceptions.AuthError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ErrorResponse {

	private String code;
	private String description;
	private String message;
	
	public ErrorResponse(final AuthError error) {
		this.code = error.getCode();
		this.description = error.getDescription();
		this.message = error.getMessage();
	}

}

