package model.records;

import lombok.Builder;

@Builder
public record ErrorResponse(
	String code,
	String description,
	String message
) {}
