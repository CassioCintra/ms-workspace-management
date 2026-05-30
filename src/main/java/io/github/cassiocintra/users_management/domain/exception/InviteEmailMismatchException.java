package io.github.cassiocintra.users_management.domain.exception;

public class InviteEmailMismatchException extends RuntimeException {

    public InviteEmailMismatchException() {
        super("Authenticated user email does not match the invited email");
    }
}
