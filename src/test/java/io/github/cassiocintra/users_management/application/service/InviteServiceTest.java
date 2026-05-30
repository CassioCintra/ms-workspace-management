package io.github.cassiocintra.users_management.application.service;

import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.application.port.in.InviteUseCase.CreateInviteCommand;
import io.github.cassiocintra.users_management.application.port.out.EmailPort;
import io.github.cassiocintra.users_management.application.port.out.InviteEventPublisher;
import io.github.cassiocintra.users_management.application.port.out.InviteRepository;
import io.github.cassiocintra.users_management.application.port.out.InviteTokenRepository;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceRepository;
import io.github.cassiocintra.users_management.domain.invite.Invite;
import io.github.cassiocintra.users_management.domain.invite.InviteStatus;
import io.github.cassiocintra.users_management.domain.workspace.Workspace;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceRole;
import io.github.cassiocintra.users_management.domain.exception.InviteAlreadyPendingException;
import io.github.cassiocintra.users_management.domain.exception.WorkspaceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock private InviteRepository inviteRepository;
    @Mock private InviteTokenRepository inviteTokenRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private InviteEventPublisher inviteEventPublisher;
    @Mock private EmailPort emailPort;

    @InjectMocks
    private InviteService service;

    private final UUID workspaceId = UUID.randomUUID();

    private Workspace workspace() {
        return Workspace.builder()
                .id(workspaceId).name("Acme").slug("acme")
                .ownerId("user-1").createdAt(Instant.now()).build();
    }

    @BeforeEach
    void setUp() {
        TenantContext.setUserId("user-1");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateInviteAndSendEmailAndPublishEvent() {
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace()));
        when(inviteRepository.existsByEmailAndStatus("bob@example.com", InviteStatus.PENDING)).thenReturn(false);
        when(inviteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Invite result = service.createInvite(new CreateInviteCommand(workspaceId, "bob@example.com", WorkspaceRole.EDITOR));

        assertThat(result.getEmail()).isEqualTo("bob@example.com");
        assertThat(result.getRole()).isEqualTo(WorkspaceRole.EDITOR);
        assertThat(result.getStatus()).isEqualTo(InviteStatus.PENDING);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiresAt()).isAfter(Instant.now());

        verify(emailPort).sendInvite(any(Invite.class), eq("Acme"), any());
        verify(inviteEventPublisher).publish(any(Invite.class), eq(workspaceId), eq("user-1"));
    }

    @Test
    void shouldThrowWhenWorkspaceNotFound() {
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createInvite(
                new CreateInviteCommand(workspaceId, "bob@example.com", WorkspaceRole.VIEWER)))
                .isInstanceOf(WorkspaceNotFoundException.class);

        verifyNoInteractions(inviteRepository, emailPort, inviteEventPublisher);
    }

    @Test
    void shouldThrowWhenPendingInviteAlreadyExists() {
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace()));
        when(inviteRepository.existsByEmailAndStatus("bob@example.com", InviteStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> service.createInvite(
                new CreateInviteCommand(workspaceId, "bob@example.com", WorkspaceRole.EDITOR)))
                .isInstanceOf(InviteAlreadyPendingException.class);

        verify(inviteRepository, never()).save(any());
        verifyNoInteractions(emailPort, inviteEventPublisher);
    }
}
