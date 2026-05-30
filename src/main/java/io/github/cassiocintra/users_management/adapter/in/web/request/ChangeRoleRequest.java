package io.github.cassiocintra.users_management.adapter.in.web.request;

import io.github.cassiocintra.users_management.domain.workspace.WorkspaceRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull WorkspaceRole role
) {}
