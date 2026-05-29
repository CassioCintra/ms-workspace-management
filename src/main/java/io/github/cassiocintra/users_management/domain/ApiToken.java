package io.github.cassiocintra.users_management.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ApiToken {

    private UUID id;
    private String name;
    private String tokenHash;
    private Instant lastUsedAt;
    private Instant revokedAt;
    private Instant createdAt;

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public ApiToken revoke() {
        return ApiToken.builder()
                .id(id)
                .name(name)
                .tokenHash(tokenHash)
                .lastUsedAt(lastUsedAt)
                .revokedAt(Instant.now())
                .createdAt(createdAt)
                .build();
    }
}
