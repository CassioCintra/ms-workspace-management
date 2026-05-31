package io.github.cassiocintra.workspace_management.domain.exception;

import java.util.UUID;

public class WorkspaceNotFoundException extends RuntimeException {

    private WorkspaceNotFoundException(String message) {
        super(message);
    }

    public static WorkspaceNotFoundException notFound(UUID id) {
        return new WorkspaceNotFoundException("Workspace not found: " + id);
    }
}
