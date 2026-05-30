package io.github.cassiocintra.users_management.application.port.out;

import io.github.cassiocintra.users_management.domain.Invite;

public interface EmailPort {

    void sendInvite(Invite invite, String workspaceName, String inviterName);
}
