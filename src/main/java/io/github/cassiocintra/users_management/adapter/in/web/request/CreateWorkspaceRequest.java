package io.github.cassiocintra.users_management.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateWorkspaceRequest(
        @NotBlank String name,
        @NotBlank String slug
) {}
