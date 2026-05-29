package io.github.cassiocintra.users_management.application.port.out;

import java.util.UUID;

public interface TenantSchemaPort {

    void provisionWorkspace(UUID workspaceId);
}
