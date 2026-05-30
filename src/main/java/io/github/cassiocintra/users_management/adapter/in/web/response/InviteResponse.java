package io.github.cassiocintra.users_management.adapter.in.web.response;

import io.github.cassiocintra.users_management.domain.invite.Invite;
import io.github.cassiocintra.users_management.domain.invite.InviteStatus;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceRole;

import java.time.Instant;
import java.util.UUID;

public record InviteResponse(
        UUID id,
        String email,
        WorkspaceRole role,
        InviteStatus status,
        Instant expiresAt,
        Instant createdAt
) {
    public static InviteResponse from(Invite invite) {
        return new InviteResponse(
                invite.getId(),
                invite.getEmail(),
                invite.getRole(),
                invite.getStatus(),
                invite.getExpiresAt(),
                invite.getCreatedAt()
        );
    }
}
