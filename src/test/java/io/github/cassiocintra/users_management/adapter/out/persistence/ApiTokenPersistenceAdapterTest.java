package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.TestcontainersConfiguration;
import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.domain.ApiToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ApiTokenPersistenceAdapterTest {

    private UUID workspaceId;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    ApiTokenPersistenceAdapter adapter;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO workspaces (id, name, slug, owner_id) VALUES (?, ?, ?, ?)",
                workspaceId, "Test Workspace", "test-" + workspaceId, "owner-1");
        TenantContext.setWorkspaceId(workspaceId.toString());
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM api_tokens WHERE workspace_id = ?", workspaceId);
        jdbcTemplate.update("DELETE FROM workspaces WHERE id = ?", workspaceId);
        TenantContext.clear();
    }

    private ApiToken token(String name, String hash) {
        return ApiToken.builder()
                .id(UUID.randomUUID()).name(name).tokenHash(hash)
                .createdAt(Instant.now()).build();
    }

    @Test
    void shouldSaveAndFindAllTokens() {
        adapter.save(token("ci-token", "hash-abc"));
        adapter.save(token("deploy-token", "hash-def"));

        List<ApiToken> tokens = adapter.findAll();

        assertThat(tokens).hasSize(2)
                .extracting(ApiToken::getName)
                .containsExactlyInAnyOrder("ci-token", "deploy-token");
    }

    @Test
    void shouldFindTokenById() {
        ApiToken saved = adapter.save(token("ci-token", "hash-abc"));

        Optional<ApiToken> found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ci-token");
        assertThat(found.get().isRevoked()).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenTokenNotFound() {
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldPersistRevokedAtOnRevoke() {
        ApiToken saved = adapter.save(token("ci-token", "hash-abc"));

        adapter.save(saved.revoke());

        Optional<ApiToken> found = adapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isRevoked()).isTrue();
        assertThat(found.get().getRevokedAt()).isNotNull();
    }

    @Test
    void shouldReturnEmptyListWhenNoTokens() {
        assertThat(adapter.findAll()).isEmpty();
    }
}
