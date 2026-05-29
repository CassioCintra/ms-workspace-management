package io.github.cassiocintra.users_management.application.port.out;

import io.github.cassiocintra.users_management.domain.Invite;
import io.github.cassiocintra.users_management.domain.InviteStatus;

import java.util.Optional;

public interface InviteRepository {

    Invite save(Invite invite);

    Optional<Invite> findByToken(String token);

    boolean existsByEmailAndStatus(String email, InviteStatus status);
}
