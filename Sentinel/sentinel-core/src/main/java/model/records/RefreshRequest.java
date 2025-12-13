package model.records;

import java.util.UUID;

import lombok.Builder;

@Builder
public record RefreshRequest(
	UUID refreshToken
) {}
