package io.github.cassiocintra.users_management.application.service;

import io.github.cassiocintra.users_management.application.port.in.WorkspaceUseCase;
import io.github.cassiocintra.users_management.application.port.out.TenantSchemaPort;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceMemberRepository;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceRepository;
import io.github.cassiocintra.users_management.domain.Workspace;
import io.github.cassiocintra.users_management.domain.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.WorkspaceRole;
import io.github.cassiocintra.users_management.domain.exception.MemberNotFoundException;
import io.github.cassiocintra.users_management.domain.exception.WorkspaceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WorkspaceService implements WorkspaceUseCase {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final TenantSchemaPort tenantSchemaPort;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository workspaceMemberRepository,
                            TenantSchemaPort tenantSchemaPort) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.tenantSchemaPort = tenantSchemaPort;
    }

    @Override
    public Workspace createWorkspace(CreateWorkspaceCommand command) {
        Workspace workspace = Workspace.builder()
                .id(UUID.randomUUID())
                .name(command.name())
                .slug(command.slug())
                .ownerId(command.ownerId())
                .createdAt(Instant.now())
                .build();

        Workspace saved = workspaceRepository.save(workspace);

        tenantSchemaPort.provisionWorkspace(saved.getId());

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .userId(command.ownerId())
                .role(WorkspaceRole.ADMIN)
                .joinedAt(Instant.now())
                .build());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMember> listMembers(UUID workspaceId) {
        requireWorkspaceExists(workspaceId);
        return workspaceMemberRepository.findAll();
    }

    @Override
    public void changeMemberRole(ChangeMemberRoleCommand command) {
        requireWorkspaceExists(command.workspaceId());
        WorkspaceMember member = workspaceMemberRepository.findByUserId(command.userId())
                .orElseThrow(() -> new MemberNotFoundException(command.userId()));
        workspaceMemberRepository.save(member.withRole(command.role()));
    }

    @Override
    public void removeMember(RemoveMemberCommand command) {
        requireWorkspaceExists(command.workspaceId());
        workspaceMemberRepository.deleteByUserId(command.userId());
    }

    private void requireWorkspaceExists(UUID workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));
    }
}
