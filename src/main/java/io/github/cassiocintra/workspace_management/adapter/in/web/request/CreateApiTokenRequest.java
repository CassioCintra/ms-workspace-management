package io.github.cassiocintra.workspace_management.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateApiTokenRequest(
        @NotBlank String name
) {}
