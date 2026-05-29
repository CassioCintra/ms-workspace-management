package io.github.cassiocintra.users_management.domain.exception;

import java.util.UUID;

public class WorkspaceNotFoundException extends RuntimeException {

    public WorkspaceNotFoundException(UUID id) {
        super("Workspace not found: " + id);
    }
}
