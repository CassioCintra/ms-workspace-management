package io.github.cassiocintra.users_management.adapter.in.web.request;

import io.github.cassiocintra.users_management.domain.workspace.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInviteRequest(
        @NotBlank @Email String email,
        @NotNull WorkspaceRole role
) {}
