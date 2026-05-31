package io.github.cassiocintra.workspace_management.application.port.out;

import io.github.cassiocintra.workspace_management.domain.token.ApiToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiTokenRepository {

    ApiToken save(ApiToken token);

    List<ApiToken> findAll();

    Optional<ApiToken> findById(UUID id);
}
