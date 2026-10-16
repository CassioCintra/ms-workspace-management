package io.github.cassiocintra.workspace_management.domain.exception;

public class WorkspaceSlugAlreadyExistsException extends RuntimeException {

    private WorkspaceSlugAlreadyExistsException(String message) {
        super(message);
    }

    public static WorkspaceSlugAlreadyExistsException alreadyExists(String slug) {
        return new WorkspaceSlugAlreadyExistsException("Workspace slug already exists: " + slug);
    }
}
