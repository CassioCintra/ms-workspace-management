package io.github.cassiocintra.workspace_management.domain.exception;

import java.util.UUID;

public class ApiTokenAlreadyRevokedException extends RuntimeException {

    private ApiTokenAlreadyRevokedException(String message) {
        super(message);
    }

    public static ApiTokenAlreadyRevokedException alreadyRevoked(UUID id) {
        return new ApiTokenAlreadyRevokedException("API token already revoked: " + id);
    }
}
