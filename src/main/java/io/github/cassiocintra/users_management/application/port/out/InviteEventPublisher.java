package io.github.cassiocintra.users_management.application.port.out;

import io.github.cassiocintra.users_management.domain.invite.Invite;

import java.util.UUID;

public interface InviteEventPublisher {

    void publish(Invite invite, UUID workspaceId, String invitedBy);
}
