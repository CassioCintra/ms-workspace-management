package io.github.cassiocintra.workspace_management.domain.exception;

import java.util.UUID;

public class ApiTokenNotFoundException extends RuntimeException {

    private ApiTokenNotFoundException(String message) {
        super(message);
    }

    public static ApiTokenNotFoundException notFound(UUID id) {
        return new ApiTokenNotFoundException("API token not found: " + id);
    }
}
