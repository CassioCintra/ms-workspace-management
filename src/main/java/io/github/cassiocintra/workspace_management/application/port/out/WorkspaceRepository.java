package io.github.cassiocintra.workspace_management.application.port.out;

import io.github.cassiocintra.workspace_management.domain.workspace.Workspace;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {

    Workspace save(Workspace workspace);

    Optional<Workspace> findById(UUID id);
}
