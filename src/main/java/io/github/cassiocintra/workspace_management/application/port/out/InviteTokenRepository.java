package io.github.cassiocintra.workspace_management.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface InviteTokenRepository {

    void save(UUID workspaceId, String token);

    Optional<UUID> findWorkspaceIdByToken(String token);

    void deleteByToken(String token);
}
