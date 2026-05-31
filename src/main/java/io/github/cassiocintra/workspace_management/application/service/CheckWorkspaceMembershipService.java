package io.github.cassiocintra.workspace_management.application.service;

import io.github.cassiocintra.workspace_management.application.port.in.CheckWorkspaceMembershipUseCase;
import io.github.cassiocintra.workspace_management.application.port.out.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CheckWorkspaceMembershipService implements CheckWorkspaceMembershipUseCase {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public CheckWorkspaceMembershipService(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Override
    public boolean isMember(UUID workspaceId, String userId) {
        return workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }
}
