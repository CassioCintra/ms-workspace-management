package io.github.cassiocintra.workspace_management.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.cassiocintra.workspace_management.domain.token.ApiToken;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiTokenResponse(
        UUID id,
        String name,
        String plainToken,
        Instant lastUsedAt,
        Instant revokedAt,
        Instant createdAt
) {
    public static ApiTokenResponse from(ApiToken token) {
        return new ApiTokenResponse(
                token.getId(),
                token.getName(),
                null,
                token.getLastUsedAt(),
                token.getRevokedAt(),
                token.getCreatedAt()
        );
    }

    public static ApiTokenResponse fromCreated(ApiToken token, String plainToken) {
        return new ApiTokenResponse(
                token.getId(),
                token.getName(),
                plainToken,
                token.getLastUsedAt(),
                token.getRevokedAt(),
                token.getCreatedAt()
        );
    }
}
