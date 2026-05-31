package io.github.cassiocintra.workspace_management.adapter.in.web.response;

import io.github.cassiocintra.workspace_management.domain.workspace.Workspace;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name,
        String slug,
        String ownerId,
        Instant createdAt
) {
    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getSlug(),
                workspace.getOwnerId(),
                workspace.getCreatedAt()
        );
    }
}
