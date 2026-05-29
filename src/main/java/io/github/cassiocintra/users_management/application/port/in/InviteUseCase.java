package io.github.cassiocintra.users_management.application.port.in;

import io.github.cassiocintra.users_management.domain.Invite;
import io.github.cassiocintra.users_management.domain.WorkspaceRole;

import java.util.UUID;

public interface InviteUseCase {

    record CreateInviteCommand(UUID workspaceId, String email, WorkspaceRole role) {}

    Invite createInvite(CreateInviteCommand command);
}
