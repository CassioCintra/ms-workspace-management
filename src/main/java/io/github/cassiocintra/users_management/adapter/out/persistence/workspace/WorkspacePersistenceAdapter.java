package io.github.cassiocintra.users_management.adapter.out.persistence.workspace;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.WorkspaceEntity;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceRepository;
import io.github.cassiocintra.users_management.domain.workspace.Workspace;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class WorkspacePersistenceAdapter implements WorkspaceRepository {

    private final WorkspaceJpaRepository jpaRepository;

    public WorkspacePersistenceAdapter(WorkspaceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Workspace save(Workspace workspace) {
        return jpaRepository.save(WorkspaceEntity.from(workspace)).toDomain();
    }

    @Override
    public Optional<Workspace> findById(UUID id) {
        return jpaRepository.findById(id).map(WorkspaceEntity::toDomain);
    }
}
