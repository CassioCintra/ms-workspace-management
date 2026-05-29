package io.github.cassiocintra.users_management.application.service;

import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.application.port.in.ApiTokenUseCase;
import io.github.cassiocintra.users_management.application.port.out.ApiTokenRepository;
import io.github.cassiocintra.users_management.application.port.out.TokenEventPublisher;
import io.github.cassiocintra.users_management.domain.ApiToken;
import io.github.cassiocintra.users_management.domain.exception.ApiTokenAlreadyRevokedException;
import io.github.cassiocintra.users_management.domain.exception.ApiTokenNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApiTokenService implements ApiTokenUseCase {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ApiTokenRepository apiTokenRepository;
    private final TokenEventPublisher tokenEventPublisher;

    public ApiTokenService(ApiTokenRepository apiTokenRepository,
                           TokenEventPublisher tokenEventPublisher) {
        this.apiTokenRepository = apiTokenRepository;
        this.tokenEventPublisher = tokenEventPublisher;
    }

    @Override
    public CreatedTokenResult createToken(CreateTokenCommand command) {
        String plainToken = generateToken();

        ApiToken token = ApiToken.builder()
                .id(UUID.randomUUID())
                .name(command.name())
                .tokenHash(hash(plainToken))
                .createdAt(Instant.now())
                .build();

        return new CreatedTokenResult(apiTokenRepository.save(token), plainToken);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiToken> listTokens() {
        return apiTokenRepository.findAll();
    }

    @Override
    public void revokeToken(UUID id) {
        ApiToken token = apiTokenRepository.findById(id)
                .orElseThrow(() -> new ApiTokenNotFoundException(id));

        if (token.isRevoked()) {
            throw new ApiTokenAlreadyRevokedException(id);
        }

        ApiToken revoked = apiTokenRepository.save(token.revoke());

        String workspaceIdStr = TenantContext.getWorkspaceId();
        UUID workspaceId = workspaceIdStr != null ? UUID.fromString(workspaceIdStr) : null;
        tokenEventPublisher.publish(revoked, workspaceId, TenantContext.getUserId());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
