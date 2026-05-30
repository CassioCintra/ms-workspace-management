package io.github.cassiocintra.users_management.application.port.out;

import io.github.cassiocintra.users_management.domain.token.ApiToken;

import java.util.UUID;

public interface TokenEventPublisher {

    void publish(ApiToken token, UUID workspaceId, String revokedBy);
}
