package io.github.cassiocintra.users_management.application.port.in;

import io.github.cassiocintra.users_management.domain.Workspace;
import io.github.cassiocintra.users_management.domain.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.WorkspaceRole;

import java.util.List;
import java.util.UUID;

public interface WorkspaceUseCase {

    record CreateWorkspaceCommand(String name, String slug, String ownerId) {}

    record ChangeMemberRoleCommand(UUID workspaceId, String userId, WorkspaceRole role) {}

    record RemoveMemberCommand(UUID workspaceId, String userId) {}

    Workspace createWorkspace(CreateWorkspaceCommand command);

    List<WorkspaceMember> listMembers(UUID workspaceId);

    void changeMemberRole(ChangeMemberRoleCommand command);

    void removeMember(RemoveMemberCommand command);
}
