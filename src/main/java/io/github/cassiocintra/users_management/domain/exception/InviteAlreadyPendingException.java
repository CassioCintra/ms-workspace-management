package io.github.cassiocintra.users_management.domain.exception;

public class InviteAlreadyPendingException extends RuntimeException {

    public InviteAlreadyPendingException(String email) {
        super("Pending invite already exists for: " + email);
    }
}
