package io.github.cassiocintra.users_management.adapter.in.web.response;

import io.github.cassiocintra.users_management.domain.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.WorkspaceRole;

import java.time.Instant;

public record WorkspaceMemberResponse(
        String userId,
        String email,
        String name,
        WorkspaceRole role,
        Instant joinedAt
) {
    public static WorkspaceMemberResponse from(WorkspaceMember member) {
        return new WorkspaceMemberResponse(
                member.getUserId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
