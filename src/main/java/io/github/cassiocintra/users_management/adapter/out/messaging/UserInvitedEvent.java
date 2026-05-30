package io.github.cassiocintra.users_management.adapter.out.messaging;

import io.github.cassiocintra.users_management.domain.invite.Invite;

import java.time.Instant;
import java.util.UUID;

public record UserInvitedEvent(
        String action,
        String workspaceId,
        String inviteeEmail,
        String role,
        String invitedBy,
        Instant invitedAt
) {
    public static UserInvitedEvent from(Invite invite, UUID workspaceId, String invitedBy) {
        return new UserInvitedEvent(
                "INVITED",
                workspaceId.toString(),
                invite.getEmail(),
                invite.getRole().name(),
                invitedBy,
                invite.getCreatedAt()
        );
    }
}
