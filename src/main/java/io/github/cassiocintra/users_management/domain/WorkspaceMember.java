package io.github.cassiocintra.users_management.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class WorkspaceMember {

    private String userId;
    private WorkspaceRole role;
    private Instant joinedAt;

    public WorkspaceMember withRole(WorkspaceRole newRole) {
        return WorkspaceMember.builder()
                .userId(this.userId)
                .role(newRole)
                .joinedAt(this.joinedAt)
                .build();
    }
}
