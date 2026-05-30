package io.github.cassiocintra.users_management.domain.exception;

public class InviteTokenNotFoundException extends RuntimeException {

    private InviteTokenNotFoundException(String message) {
        super(message);
    }

    public static InviteTokenNotFoundException notFound(String token) {
        return new InviteTokenNotFoundException("Invite token not found or already used: " + token);
    }
}
