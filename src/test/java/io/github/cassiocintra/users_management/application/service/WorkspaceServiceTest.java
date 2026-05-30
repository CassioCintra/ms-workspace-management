package io.github.cassiocintra.users_management.application.service;

import io.github.cassiocintra.users_management.application.port.in.WorkspaceUseCase.ChangeMemberRoleCommand;
import io.github.cassiocintra.users_management.application.port.in.WorkspaceUseCase.CreateWorkspaceCommand;
import io.github.cassiocintra.users_management.application.port.in.WorkspaceUseCase.RemoveMemberCommand;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceMemberRepository;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceRepository;
import io.github.cassiocintra.users_management.domain.workspace.Workspace;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceRole;
import io.github.cassiocintra.users_management.domain.exception.MemberNotFoundException;
import io.github.cassiocintra.users_management.domain.exception.WorkspaceNotFoundException;
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
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private WorkspaceService service;

    private Workspace workspace(UUID id) {
        return Workspace.builder()
                .id(id).name("Acme").slug("acme")
                .ownerId("user-1").createdAt(Instant.now()).build();
    }

    private WorkspaceMember member(String userId, WorkspaceRole role) {
        return WorkspaceMember.builder()
                .userId(userId).role(role).joinedAt(Instant.now()).build();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Test
    void shouldCreateWorkspaceAndProvisionTenantSchema() {
        UUID id = UUID.randomUUID();
        when(workspaceRepository.save(any())).thenReturn(workspace(id));

        Workspace result = service.createWorkspace(new CreateWorkspaceCommand("Acme", "acme", "user-1", "user1@acme.com", "User One"));

        assertThat(result.getName()).isEqualTo("Acme");
    }

    @Test
    void shouldAddOwnerAsAdminWhenCreatingWorkspace() {
        UUID id = UUID.randomUUID();
        when(workspaceRepository.save(any())).thenReturn(workspace(id));

        service.createWorkspace(new CreateWorkspaceCommand("Acme", "acme", "user-1", "user1@acme.com", "User One"));

        ArgumentCaptor<WorkspaceMember> captor = ArgumentCaptor.forClass(WorkspaceMember.class);
        verify(workspaceMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("user-1");
        assertThat(captor.getValue().getRole()).isEqualTo(WorkspaceRole.ADMIN);
    }

    // ── LIST MEMBERS ──────────────────────────────────────────────────────────

    @Test
    void shouldListMembersWhenWorkspaceExists() {
        UUID id = UUID.randomUUID();
        when(workspaceRepository.findById(id)).thenReturn(Optional.of(workspace(id)));
        when(workspaceMemberRepository.findAll()).thenReturn(List.of(member("u1", WorkspaceRole.ADMIN)));

        List<WorkspaceMember> result = service.listMembers(id);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("u1");
    }

    @Test
    void shouldThrowWhenListingMembersOfNonExistentWorkspace() {
        UUID id = UUID.randomUUID();
        when(workspaceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listMembers(id))
                .isInstanceOf(WorkspaceNotFoundException.class);

        verify(workspaceMemberRepository, never()).findAll();
    }

    // ── CHANGE ROLE ───────────────────────────────────────────────────────────

    @Test
    void shouldChangeMemberRoleWhenMemberExists() {
        UUID workspaceId = UUID.randomUUID();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace(workspaceId)));
        when(workspaceMemberRepository.findByUserId("u1")).thenReturn(Optional.of(member("u1", WorkspaceRole.EDITOR)));

        service.changeMemberRole(new ChangeMemberRoleCommand(workspaceId, "u1", WorkspaceRole.ADMIN));

        ArgumentCaptor<WorkspaceMember> captor = ArgumentCaptor.forClass(WorkspaceMember.class);
        verify(workspaceMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(WorkspaceRole.ADMIN);
    }

    @Test
    void shouldThrowWhenChangingRoleOfNonExistentMember() {
        UUID workspaceId = UUID.randomUUID();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace(workspaceId)));
        when(workspaceMemberRepository.findByUserId("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeMemberRole(
                new ChangeMemberRoleCommand(workspaceId, "ghost", WorkspaceRole.ADMIN)))
                .isInstanceOf(MemberNotFoundException.class);

        verify(workspaceMemberRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenChangingRoleInNonExistentWorkspace() {
        UUID id = UUID.randomUUID();
        when(workspaceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeMemberRole(
                new ChangeMemberRoleCommand(id, "u1", WorkspaceRole.VIEWER)))
                .isInstanceOf(WorkspaceNotFoundException.class);
    }

    // ── REMOVE MEMBER ─────────────────────────────────────────────────────────

    @Test
    void shouldRemoveMemberWhenWorkspaceExists() {
        UUID workspaceId = UUID.randomUUID();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace(workspaceId)));

        service.removeMember(new RemoveMemberCommand(workspaceId, "u1"));

        verify(workspaceMemberRepository).deleteByUserId("u1");
    }

    @Test
    void shouldThrowWhenRemovingMemberFromNonExistentWorkspace() {
        UUID id = UUID.randomUUID();
        when(workspaceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeMember(new RemoveMemberCommand(id, "u1")))
                .isInstanceOf(WorkspaceNotFoundException.class);

        verify(workspaceMemberRepository, never()).deleteByUserId(any());
    }
}
