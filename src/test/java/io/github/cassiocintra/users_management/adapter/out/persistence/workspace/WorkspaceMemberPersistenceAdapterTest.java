package io.github.cassiocintra.users_management.adapter.out.persistence.workspace;

import io.github.cassiocintra.users_management.TestcontainersConfiguration;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceRole;
import io.github.cassiocintra.users_management.application.TenantContext;
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
class WorkspaceMemberPersistenceAdapterTest {

    private UUID workspaceId;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    WorkspaceMemberPersistenceAdapter adapter;

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
        jdbcTemplate.update("DELETE FROM workspace_members WHERE workspace_id = ?", workspaceId);
        jdbcTemplate.update("DELETE FROM workspaces WHERE id = ?", workspaceId);
        TenantContext.clear();
    }

    private WorkspaceMember member(String userId, WorkspaceRole role) {
        return WorkspaceMember.builder()
                .userId(userId).role(role).joinedAt(Instant.now()).build();
    }

    @Test
    void shouldSaveMemberAndFindAll() {
        adapter.save(member("user-1", WorkspaceRole.ADMIN));

        List<WorkspaceMember> members = adapter.findAll();

        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUserId()).isEqualTo("user-1");
        assertThat(members.get(0).getRole()).isEqualTo(WorkspaceRole.ADMIN);
    }

    @Test
    void shouldFindMemberByUserId() {
        adapter.save(member("user-1", WorkspaceRole.EDITOR));

        Optional<WorkspaceMember> found = adapter.findByUserId("user-1");

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(WorkspaceRole.EDITOR);
    }

    @Test
    void shouldReturnEmptyWhenMemberNotFound() {
        assertThat(adapter.findByUserId("ghost")).isEmpty();
    }

    @Test
    void shouldUpdateMemberRole() {
        WorkspaceMember m = adapter.save(member("user-1", WorkspaceRole.VIEWER));

        adapter.save(m.withRole(WorkspaceRole.ADMIN));

        assertThat(adapter.findByUserId("user-1"))
                .isPresent()
                .hasValueSatisfying(it -> assertThat(it.getRole()).isEqualTo(WorkspaceRole.ADMIN));
    }

    @Test
    void shouldDeleteMemberByUserId() {
        adapter.save(member("user-1", WorkspaceRole.VIEWER));

        adapter.deleteByUserId("user-1");

        assertThat(adapter.findByUserId("user-1")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoMembers() {
        assertThat(adapter.findAll()).isEmpty();
    }

    @Test
    void shouldIsolateMembersByWorkspace() {
        UUID otherWorkspaceId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO workspaces (id, name, slug, owner_id) VALUES (?, ?, ?, ?)",
                otherWorkspaceId, "Other Workspace", "other-" + otherWorkspaceId, "owner-2");
        jdbcTemplate.update("INSERT INTO workspace_members (workspace_id, user_id, role, joined_at) VALUES (?, ?, ?, NOW())",
                otherWorkspaceId, "user-other", "ADMIN");

        adapter.save(member("user-1", WorkspaceRole.ADMIN));

        assertThat(adapter.findAll()).hasSize(1)
                .extracting(WorkspaceMember::getUserId).containsOnly("user-1");

        jdbcTemplate.update("DELETE FROM workspace_members WHERE workspace_id = ?", otherWorkspaceId);
        jdbcTemplate.update("DELETE FROM workspaces WHERE id = ?", otherWorkspaceId);
    }
}
