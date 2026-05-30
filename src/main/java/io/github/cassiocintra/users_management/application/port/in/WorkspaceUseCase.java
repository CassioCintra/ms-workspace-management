package io.github.cassiocintra.users_management.application.port.in;

import io.github.cassiocintra.users_management.domain.workspace.Workspace;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceRole;

import java.util.List;
import java.util.UUID;

public interface WorkspaceUseCase {

    record CreateWorkspaceCommand(String name, String slug, String ownerId, String ownerEmail, String ownerName) {}

    record ChangeMemberRoleCommand(UUID workspaceId, String userId, WorkspaceRole role) {}

    record RemoveMemberCommand(UUID workspaceId, String userId) {}

    Workspace createWorkspace(CreateWorkspaceCommand command);

    List<WorkspaceMember> listMembers(UUID workspaceId);

    void changeMemberRole(ChangeMemberRoleCommand command);

    void removeMember(RemoveMemberCommand command);
}
