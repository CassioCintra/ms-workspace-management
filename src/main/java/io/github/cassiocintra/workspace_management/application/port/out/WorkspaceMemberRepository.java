package io.github.cassiocintra.workspace_management.application.port.out;

import io.github.cassiocintra.workspace_management.domain.workspace.WorkspaceMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMemberRepository {

    List<WorkspaceMember> findAll();

    Optional<WorkspaceMember> findByUserId(String userId);

    WorkspaceMember save(WorkspaceMember member);

    void deleteByUserId(String userId);

    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, String userId);
}
