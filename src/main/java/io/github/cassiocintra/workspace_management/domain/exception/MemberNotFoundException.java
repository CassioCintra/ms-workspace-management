package io.github.cassiocintra.workspace_management.domain.exception;

public class MemberNotFoundException extends RuntimeException {

    private MemberNotFoundException(String message) {
        super(message);
    }

    public static MemberNotFoundException notFound(String userId) {
        return new MemberNotFoundException("Member not found: " + userId);
    }
}
