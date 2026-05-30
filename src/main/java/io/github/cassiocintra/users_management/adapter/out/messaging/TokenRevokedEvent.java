package io.github.cassiocintra.users_management.adapter.out.messaging;

import io.github.cassiocintra.users_management.domain.token.ApiToken;

import java.time.Instant;
import java.util.UUID;

public record TokenRevokedEvent(
        String action,
        String workspaceId,
        String tokenId,
        String tokenName,
        String revokedBy,
        Instant revokedAt
) {
    public static TokenRevokedEvent from(ApiToken token, UUID workspaceId, String revokedBy) {
        return new TokenRevokedEvent(
                "REVOKED",
                workspaceId != null ? workspaceId.toString() : null,
                token.getId().toString(),
                token.getName(),
                revokedBy,
                token.getRevokedAt()
        );
    }
}
