package io.github.cassiocintra.workspace_management.domain.exception;

public class InviteExpiredException extends RuntimeException {

    private InviteExpiredException(String message) {
        super(message);
    }

    public static InviteExpiredException expired(String token) {
        return new InviteExpiredException("Invite has expired: " + token);
    }
}
