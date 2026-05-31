package io.github.cassiocintra.workspace_management.adapter.in.web.response;

import io.github.cassiocintra.workspace_management.application.port.in.InviteUseCase.InviteInfo;
import io.github.cassiocintra.workspace_management.domain.invite.InviteStatus;
import io.github.cassiocintra.workspace_management.domain.workspace.WorkspaceRole;

import java.time.Instant;

public record InviteInfoResponse(
        String email,
        String workspaceName,
        WorkspaceRole role,
        Instant expiresAt,
        InviteStatus status
) {
    public static InviteInfoResponse from(InviteInfo info) {
        return new InviteInfoResponse(
                info.email(),
                info.workspaceName(),
                info.role(),
                info.expiresAt(),
                info.status()
        );
    }
}
