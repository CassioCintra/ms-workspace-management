package io.github.cassiocintra.users_management.adapter.out.persistence.workspace;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.WorkspaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkspaceJpaRepository extends JpaRepository<WorkspaceEntity, UUID> {}
