package io.github.cassiocintra.workspace_management.domain.exception;

public class InviteEmailMismatchException extends RuntimeException {

    private InviteEmailMismatchException(String message) {
        super(message);
    }

    public static InviteEmailMismatchException mismatch() {
        return new InviteEmailMismatchException("Authenticated user email does not match the invited email");
    }
}
