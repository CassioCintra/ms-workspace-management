package io.github.cassiocintra.users_management.domain.exception;

import java.util.UUID;

public class ApiTokenNotFoundException extends RuntimeException {

    public ApiTokenNotFoundException(UUID id) {
        super("API token not found: " + id);
    }
}
