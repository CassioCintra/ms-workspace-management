package io.github.cassiocintra.workspace_management.adapter.out.persistence.token;

import io.github.cassiocintra.workspace_management.adapter.out.persistence.entity.ApiTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiTokenJpaRepository extends JpaRepository<ApiTokenEntity, UUID> {

    List<ApiTokenEntity> findAllByWorkspaceId(UUID workspaceId);

    Optional<ApiTokenEntity> findByWorkspaceIdAndId(UUID workspaceId, UUID id);
}
