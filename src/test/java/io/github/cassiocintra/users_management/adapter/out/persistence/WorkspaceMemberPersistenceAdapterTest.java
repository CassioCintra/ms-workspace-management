package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.TestcontainersConfiguration;
import io.github.cassiocintra.users_management.domain.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.WorkspaceRole;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class WorkspaceMemberPersistenceAdapterTest {

    private static final String SCHEMA = "ws_test_members";

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    WorkspaceMemberPersistenceAdapter adapter;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + SCHEMA);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS %s.workspace_members (
                    user_id   VARCHAR(255) NOT NULL PRIMARY KEY,
                    role      VARCHAR(50)  NOT NULL,
                    joined_at TIMESTAMP    NOT NULL DEFAULT NOW()
                )""".formatted(SCHEMA));
        jdbcTemplate.execute("TRUNCATE TABLE " + SCHEMA + ".workspace_members");
        TenantContext.setTenantId(SCHEMA);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldSaveMemberAndFindAll() {
        WorkspaceMember member = WorkspaceMember.builder()
                .userId("user-1").role(WorkspaceRole.ADMIN).joinedAt(Instant.now()).build();

        adapter.save(member);
        List<WorkspaceMember> members = adapter.findAll();

        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUserId()).isEqualTo("user-1");
        assertThat(members.get(0).getRole()).isEqualTo(WorkspaceRole.ADMIN);
    }

    @Test
    void shouldFindMemberByUserId() {
        adapter.save(WorkspaceMember.builder().userId("user-1").role(WorkspaceRole.EDITOR).joinedAt(Instant.now()).build());

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
        WorkspaceMember member = WorkspaceMember.builder()
                .userId("user-1").role(WorkspaceRole.VIEWER).joinedAt(Instant.now()).build();
        adapter.save(member);

        adapter.save(member.withRole(WorkspaceRole.ADMIN));

        assertThat(adapter.findByUserId("user-1"))
                .isPresent()
                .hasValueSatisfying(m -> assertThat(m.getRole()).isEqualTo(WorkspaceRole.ADMIN));
    }

    @Test
    void shouldDeleteMemberByUserId() {
        adapter.save(WorkspaceMember.builder().userId("user-1").role(WorkspaceRole.VIEWER).joinedAt(Instant.now()).build());

        adapter.deleteByUserId("user-1");

        assertThat(adapter.findByUserId("user-1")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoMembers() {
        assertThat(adapter.findAll()).isEmpty();
    }
}
