package io.github.cassiocintra.workspace_management.adapter.out.persistence.workspace;

import io.github.cassiocintra.workspace_management.adapter.out.persistence.entity.WorkspaceMemberEntity;
import io.github.cassiocintra.workspace_management.application.TenantContext;
import io.github.cassiocintra.workspace_management.application.port.out.WorkspaceMemberRepository;
import io.github.cassiocintra.workspace_management.domain.workspace.WorkspaceMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WorkspaceMemberPersistenceAdapter implements WorkspaceMemberRepository {

    private final WorkspaceMemberJpaRepository jpaRepository;

    public WorkspaceMemberPersistenceAdapter(WorkspaceMemberJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<WorkspaceMember> findAll() {
        return jpaRepository.findAllByIdWorkspaceId(workspaceId())
                .stream().map(WorkspaceMemberEntity::toDomain).toList();
    }

    @Override
    public Optional<WorkspaceMember> findByUserId(String userId) {
        return jpaRepository.findByIdWorkspaceIdAndIdUserId(workspaceId(), userId)
                .map(WorkspaceMemberEntity::toDomain);
    }

    @Override
    public WorkspaceMember save(WorkspaceMember member) {
        return jpaRepository.save(WorkspaceMemberEntity.from(workspaceId(), member)).toDomain();
    }

    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByWorkspaceIdAndUserId(workspaceId(), userId);
    }

    @Override
    public boolean existsByWorkspaceIdAndUserId(UUID workspaceId, String userId) {
        return jpaRepository.existsByIdWorkspaceIdAndIdUserId(workspaceId, userId);
    }

    private UUID workspaceId() {
        String id = TenantContext.getWorkspaceId();
        if (id == null) throw new IllegalStateException("workspaceId not set in TenantContext");
        return UUID.fromString(id);
    }
}
