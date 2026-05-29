package io.github.cassiocintra.users_management.application.port.out;

import io.github.cassiocintra.users_management.domain.Workspace;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {

    Workspace save(Workspace workspace);

    Optional<Workspace> findById(UUID id);
}
