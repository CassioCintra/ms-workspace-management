package io.github.cassiocintra.workspace_management.domain.workspace;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class WorkspaceMember {

    private String userId;
    private String email;
    private String name;
    private WorkspaceRole role;
    private Instant joinedAt;

    public WorkspaceMember withRole(WorkspaceRole newRole) {
        return WorkspaceMember.builder()
                .userId(this.userId)
                .email(this.email)
                .name(this.name)
                .role(newRole)
                .joinedAt(this.joinedAt)
                .build();
    }
}
