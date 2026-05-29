package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.TestcontainersConfiguration;
import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.domain.Invite;
import io.github.cassiocintra.users_management.domain.InviteStatus;
import io.github.cassiocintra.users_management.domain.WorkspaceRole;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class InvitePersistenceAdapterTest {

    private static final String SCHEMA = "ws_test_invites";

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    InvitePersistenceAdapter adapter;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + SCHEMA);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS %s.invites (
                    id         UUID         PRIMARY KEY,
                    email      VARCHAR(255) NOT NULL,
                    role       VARCHAR(50)  NOT NULL,
                    token      VARCHAR(255) NOT NULL UNIQUE,
                    expires_at TIMESTAMP    NOT NULL,
                    status     VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
                    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
                )""".formatted(SCHEMA));
        jdbcTemplate.execute("TRUNCATE TABLE " + SCHEMA + ".invites");
        TenantContext.setTenantId(SCHEMA);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Invite invite(String email, String token) {
        return Invite.builder()
                .id(UUID.randomUUID())
                .email(email)
                .role(WorkspaceRole.EDITOR)
                .token(token)
                .expiresAt(Instant.now().plusSeconds(259200))
                .status(InviteStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void shouldSaveAndFindInviteByToken() {
        String token = UUID.randomUUID().toString();
        adapter.save(invite("bob@example.com", token));

        Optional<Invite> found = adapter.findByToken(token);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("bob@example.com");
        assertThat(found.get().getRole()).isEqualTo(WorkspaceRole.EDITOR);
        assertThat(found.get().getStatus()).isEqualTo(InviteStatus.PENDING);
    }

    @Test
    void shouldReturnEmptyWhenTokenNotFound() {
        assertThat(adapter.findByToken("non-existent-token")).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenPendingInviteExistsForEmail() {
        adapter.save(invite("bob@example.com", UUID.randomUUID().toString()));

        assertThat(adapter.existsByEmailAndStatus("bob@example.com", InviteStatus.PENDING)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoPendingInviteForEmail() {
        assertThat(adapter.existsByEmailAndStatus("ghost@example.com", InviteStatus.PENDING)).isFalse();
    }
}
