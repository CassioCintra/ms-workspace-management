package io.github.cassiocintra.workspace_management.application.port.in;

import io.github.cassiocintra.workspace_management.domain.token.ApiToken;

import java.util.List;
import java.util.UUID;

public interface ApiTokenUseCase {

    record CreateTokenCommand(String name) {}

    record CreatedTokenResult(ApiToken token, String plainToken) {}

    CreatedTokenResult createToken(CreateTokenCommand command);

    List<ApiToken> listTokens();

    void revokeToken(UUID id);
}
