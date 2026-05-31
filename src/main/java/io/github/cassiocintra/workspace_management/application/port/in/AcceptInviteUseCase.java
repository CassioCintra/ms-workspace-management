package io.github.cassiocintra.workspace_management.application.port.in;

public interface AcceptInviteUseCase {

    record AcceptInviteCommand(String token) {}

    void acceptInvite(AcceptInviteCommand command);
}
