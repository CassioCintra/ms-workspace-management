package io.github.cassiocintra.users_management.adapter.out.persistence.entity;

import io.github.cassiocintra.users_management.domain.Invite;
import io.github.cassiocintra.users_management.domain.InviteStatus;
import io.github.cassiocintra.users_management.domain.WorkspaceRole;
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

    public static InviteEntity from(Invite invite) {
        return InviteEntity.builder()
                .id(invite.getId())
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
