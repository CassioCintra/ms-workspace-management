package io.github.cassiocintra.users_management.domain.exception;

public class InviteTokenNotFoundException extends RuntimeException {

    public InviteTokenNotFoundException(String token) {
        super("Invite token not found or already used: " + token);
    }
}
