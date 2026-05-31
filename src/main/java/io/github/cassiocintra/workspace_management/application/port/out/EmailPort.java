package io.github.cassiocintra.workspace_management.application.port.out;

import io.github.cassiocintra.workspace_management.domain.invite.Invite;

public interface EmailPort {

    void sendInvite(Invite invite, String workspaceName, String inviterName);
}
