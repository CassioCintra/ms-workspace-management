package io.github.cassiocintra.workspace_management.application.port.in;

import java.util.UUID;

public interface CheckWorkspaceMembershipUseCase {

    boolean isMember(UUID workspaceId, String userId);
}
