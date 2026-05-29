package io.github.cassiocintra.users_management.application.port.in;

import io.github.cassiocintra.users_management.domain.ApiToken;

import java.util.List;
import java.util.UUID;

public interface ApiTokenUseCase {

    record CreateTokenCommand(String name) {}

    record CreatedTokenResult(ApiToken token, String plainToken) {}

    CreatedTokenResult createToken(CreateTokenCommand command);

    List<ApiToken> listTokens();

    void revokeToken(UUID id);
}
