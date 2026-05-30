package io.github.cassiocintra.users_management.domain.exception;

public class InviteExpiredException extends RuntimeException {

    public InviteExpiredException(String token) {
        super("Invite has expired: " + token);
    }
}
