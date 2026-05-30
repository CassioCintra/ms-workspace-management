package io.github.cassiocintra.users_management.domain.exception;

public class InviteAlreadyPendingException extends RuntimeException {

    private InviteAlreadyPendingException(String message) {
        super(message);
    }

    public static InviteAlreadyPendingException alreadyPending(String email) {
        return new InviteAlreadyPendingException("Pending invite already exists for: " + email);
    }
}
