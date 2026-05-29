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

    private static final String SCHEMA = "ws_test_tokens";

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    ApiTokenPersistenceAdapter adapter;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + SCHEMA);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS %s.api_tokens (
                    id           UUID         PRIMARY KEY,
                    name         VARCHAR(255) NOT NULL,
                    token_hash   VARCHAR(255) NOT NULL UNIQUE,
                    last_used_at TIMESTAMP,
                    revoked_at   TIMESTAMP,
                    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
                )""".formatted(SCHEMA));
        jdbcTemplate.execute("TRUNCATE TABLE " + SCHEMA + ".api_tokens");
        TenantContext.setTenantId(SCHEMA);
    }

    @AfterEach
    void tearDown() {
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
