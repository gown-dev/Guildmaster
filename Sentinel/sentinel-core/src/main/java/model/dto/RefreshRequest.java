package model.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class RefreshRequest {

	private UUID refreshToken;
	
}
