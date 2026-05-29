package io.github.cassiocintra.users_management.domain.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String userId) {
        super("Member not found: " + userId);
    }
}
