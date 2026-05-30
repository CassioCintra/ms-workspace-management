package io.github.cassiocintra.users_management.application.port.in;

public interface AcceptInviteUseCase {

    record AcceptInviteCommand(String token) {}

    void acceptInvite(AcceptInviteCommand command);
}
