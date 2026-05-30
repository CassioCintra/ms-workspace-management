package io.github.cassiocintra.users_management.application.port.in;

import io.github.cassiocintra.users_management.domain.Invite;
import io.github.cassiocintra.users_management.domain.InviteStatus;
import io.github.cassiocintra.users_management.domain.WorkspaceRole;

import java.time.Instant;
import java.util.UUID;

public interface InviteUseCase {

    record CreateInviteCommand(UUID workspaceId, String email, WorkspaceRole role) {}

    record InviteInfo(String email, String workspaceName, WorkspaceRole role, Instant expiresAt, InviteStatus status) {}

    Invite createInvite(CreateInviteCommand command);

    InviteInfo getInviteInfo(String token);
}
