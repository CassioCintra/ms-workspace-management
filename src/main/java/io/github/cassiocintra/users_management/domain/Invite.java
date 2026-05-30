package io.github.cassiocintra.users_management.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class Invite {

    private UUID id;
    private String email;
    private WorkspaceRole role;
    private String token;
    private Instant expiresAt;
    private InviteStatus status;
    private Instant createdAt;

    public Invite accept() {
        return Invite.builder()
                .id(id).email(email).role(role).token(token)
                .expiresAt(expiresAt).status(InviteStatus.ACCEPTED)
                .createdAt(createdAt).build();
    }
}
