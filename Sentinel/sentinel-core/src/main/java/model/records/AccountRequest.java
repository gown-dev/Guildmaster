package model.records;

import lombok.Builder;

@Builder
public record AccountRequest(
    String username,
    String tag,
    String password
) {}
