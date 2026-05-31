package io.github.cassiocintra.workspace_management.application.port.out;

import io.github.cassiocintra.workspace_management.domain.invite.Invite;
import io.github.cassiocintra.workspace_management.domain.invite.InviteStatus;

import java.util.Optional;

public interface InviteRepository {

    Invite save(Invite invite);

    Optional<Invite> findByToken(String token);

    boolean existsByEmailAndStatus(String email, InviteStatus status);
}
