package io.github.cassiocintra.users_management.adapter.out.persistence.entity;

import io.github.cassiocintra.users_management.domain.invite.Invite;
import io.github.cassiocintra.users_management.domain.invite.InviteStatus;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "invites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteEntity {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static InviteEntity from(UUID workspaceId, Invite invite) {
        return InviteEntity.builder()
                .id(invite.getId())
                .workspaceId(workspaceId)
                .email(invite.getEmail())
                .role(invite.getRole())
                .token(invite.getToken())
                .expiresAt(invite.getExpiresAt())
                .status(invite.getStatus())
                .createdAt(invite.getCreatedAt())
                .build();
    }

    public Invite toDomain() {
        return Invite.builder()
                .id(id)
                .email(email)
                .role(role)
                .token(token)
                .expiresAt(expiresAt)
                .status(status)
                .createdAt(createdAt)
                .build();
    }
}
