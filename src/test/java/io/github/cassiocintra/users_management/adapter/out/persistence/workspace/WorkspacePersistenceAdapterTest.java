package io.github.cassiocintra.users_management.adapter.out.persistence.workspace;

import io.github.cassiocintra.users_management.TestcontainersConfiguration;
import io.github.cassiocintra.users_management.domain.workspace.Workspace;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
class WorkspacePersistenceAdapterTest {

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    WorkspacePersistenceAdapter adapter;

    @Test
    void shouldSaveAndFindWorkspace() {
        Workspace workspace = Workspace.builder()
                .id(UUID.randomUUID())
                .name("Acme Corp")
                .slug("acme-corp")
                .ownerId("user-1")
                .createdAt(Instant.now())
                .build();

        Workspace saved = adapter.save(workspace);
        Optional<Workspace> found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Acme Corp");
        assertThat(found.get().getSlug()).isEqualTo("acme-corp");
        assertThat(found.get().getOwnerId()).isEqualTo("user-1");
    }

    @Test
    void shouldReturnEmptyWhenWorkspaceNotFound() {
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldGenerateUniqueIdPerWorkspace() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        adapter.save(Workspace.builder().id(id1).name("W1").slug("w1").ownerId("u1").createdAt(Instant.now()).build());
        adapter.save(Workspace.builder().id(id2).name("W2").slug("w2").ownerId("u1").createdAt(Instant.now()).build());

        assertThat(adapter.findById(id1)).isPresent();
        assertThat(adapter.findById(id2)).isPresent();
    }
}
