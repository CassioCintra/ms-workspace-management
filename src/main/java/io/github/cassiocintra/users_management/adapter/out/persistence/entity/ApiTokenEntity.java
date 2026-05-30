package io.github.cassiocintra.users_management.adapter.out.persistence.entity;

import io.github.cassiocintra.users_management.domain.ApiToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiTokenEntity {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(nullable = false)
    private String name;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static ApiTokenEntity from(UUID workspaceId, ApiToken token) {
        return ApiTokenEntity.builder()
                .id(token.getId())
                .workspaceId(workspaceId)
                .name(token.getName())
                .tokenHash(token.getTokenHash())
                .lastUsedAt(token.getLastUsedAt())
                .revokedAt(token.getRevokedAt())
                .createdAt(token.getCreatedAt())
                .build();
    }

    public ApiToken toDomain() {
        return ApiToken.builder()
                .id(id)
                .name(name)
                .tokenHash(tokenHash)
                .lastUsedAt(lastUsedAt)
                .revokedAt(revokedAt)
                .createdAt(createdAt)
                .build();
    }
}
