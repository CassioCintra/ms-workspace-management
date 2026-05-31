package io.github.cassiocintra.workspace_management.adapter.out.persistence.invite;

import io.github.cassiocintra.workspace_management.TestcontainersConfiguration;
import io.github.cassiocintra.workspace_management.application.TenantContext;
import io.github.cassiocintra.workspace_management.domain.invite.Invite;
import io.github.cassiocintra.workspace_management.domain.invite.InviteStatus;
import io.github.cassiocintra.workspace_management.domain.workspace.WorkspaceRole;
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

    private UUID workspaceId;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    InvitePersistenceAdapter adapter;

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
        jdbcTemplate.update("DELETE FROM invites WHERE workspace_id = ?", workspaceId);
        jdbcTemplate.update("DELETE FROM workspaces WHERE id = ?", workspaceId);
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
