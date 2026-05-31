package io.github.cassiocintra.workspace_management.application.service;

import io.github.cassiocintra.workspace_management.application.TenantContext;
import io.github.cassiocintra.workspace_management.application.port.in.ApiTokenUseCase.CreateTokenCommand;
import io.github.cassiocintra.workspace_management.application.port.in.ApiTokenUseCase.CreatedTokenResult;
import io.github.cassiocintra.workspace_management.application.port.out.ApiTokenRepository;
import io.github.cassiocintra.workspace_management.application.port.out.TokenEventPublisher;
import io.github.cassiocintra.workspace_management.domain.token.ApiToken;
import io.github.cassiocintra.workspace_management.domain.exception.ApiTokenAlreadyRevokedException;
import io.github.cassiocintra.workspace_management.domain.exception.ApiTokenNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiTokenServiceTest {

    @Mock private ApiTokenRepository apiTokenRepository;
    @Mock private TokenEventPublisher tokenEventPublisher;

    @InjectMocks
    private ApiTokenService service;

    private final UUID workspaceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setUserId("user-1");
        TenantContext.setWorkspaceId(workspaceId.toString());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private ApiToken activeToken(UUID id) {
        return ApiToken.builder()
                .id(id).name("ci-token").tokenHash("hash-abc")
                .createdAt(Instant.now()).build();
    }

    private ApiToken revokedToken(UUID id) {
        return ApiToken.builder()
                .id(id).name("ci-token").tokenHash("hash-abc")
                .revokedAt(Instant.now()).createdAt(Instant.now()).build();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Test
    void shouldCreateTokenWithHashedSecret() {
        when(apiTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreatedTokenResult result = service.createToken(new CreateTokenCommand("ci-token"));

        assertThat(result.plainToken()).isNotBlank();
        assertThat(result.token().getName()).isEqualTo("ci-token");
        assertThat(result.token().getTokenHash()).isNotBlank();
        assertThat(result.token().getTokenHash()).isNotEqualTo(result.plainToken());
        assertThat(result.token().isRevoked()).isFalse();
    }

    @Test
    void shouldHashTokenDifferentlyFromPlainToken() {
        when(apiTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreatedTokenResult result = service.createToken(new CreateTokenCommand("my-token"));

        ArgumentCaptor<ApiToken> captor = ArgumentCaptor.forClass(ApiToken.class);
        verify(apiTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).hasSize(64); // SHA-256 hex = 64 chars
    }

    // ── LIST ──────────────────────────────────────────────────────────────────

    @Test
    void shouldListAllTokens() {
        List<ApiToken> tokens = List.of(activeToken(UUID.randomUUID()), revokedToken(UUID.randomUUID()));
        when(apiTokenRepository.findAll()).thenReturn(tokens);

        List<ApiToken> result = service.listTokens();

        assertThat(result).hasSize(2);
    }

    // ── REVOKE ────────────────────────────────────────────────────────────────

    @Test
    void shouldRevokeTokenAndPublishEvent() {
        UUID id = UUID.randomUUID();
        when(apiTokenRepository.findById(id)).thenReturn(Optional.of(activeToken(id)));
        when(apiTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.revokeToken(id);

        ArgumentCaptor<ApiToken> captor = ArgumentCaptor.forClass(ApiToken.class);
        verify(apiTokenRepository).save(captor.capture());
        assertThat(captor.getValue().isRevoked()).isTrue();
        assertThat(captor.getValue().getRevokedAt()).isNotNull();

        verify(tokenEventPublisher).publish(any(ApiToken.class), eq(workspaceId), eq("user-1"));
    }

    @Test
    void shouldThrowWhenRevokingNonExistentToken() {
        UUID id = UUID.randomUUID();
        when(apiTokenRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revokeToken(id))
                .isInstanceOf(ApiTokenNotFoundException.class);

        verify(apiTokenRepository, never()).save(any());
        verifyNoInteractions(tokenEventPublisher);
    }

    @Test
    void shouldThrowWhenRevokingAlreadyRevokedToken() {
        UUID id = UUID.randomUUID();
        when(apiTokenRepository.findById(id)).thenReturn(Optional.of(revokedToken(id)));

        assertThatThrownBy(() -> service.revokeToken(id))
                .isInstanceOf(ApiTokenAlreadyRevokedException.class);

        verify(apiTokenRepository, never()).save(any());
        verifyNoInteractions(tokenEventPublisher);
    }
}
