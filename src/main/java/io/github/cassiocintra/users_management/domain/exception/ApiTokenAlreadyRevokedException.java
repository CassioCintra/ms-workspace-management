package io.github.cassiocintra.users_management.domain.exception;

import java.util.UUID;

public class ApiTokenAlreadyRevokedException extends RuntimeException {

    public ApiTokenAlreadyRevokedException(UUID id) {
        super("API token already revoked: " + id);
    }
}
