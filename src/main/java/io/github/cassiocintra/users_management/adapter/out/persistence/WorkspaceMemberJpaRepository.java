package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.WorkspaceMemberEntity;
import io.github.cassiocintra.users_management.adapter.out.persistence.entity.WorkspaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMemberJpaRepository extends JpaRepository<WorkspaceMemberEntity, WorkspaceMemberId> {

    List<WorkspaceMemberEntity> findAllByIdWorkspaceId(UUID workspaceId);

    Optional<WorkspaceMemberEntity> findByIdWorkspaceIdAndIdUserId(UUID workspaceId, String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM WorkspaceMemberEntity m WHERE m.id.workspaceId = :workspaceId AND m.id.userId = :userId")
    void deleteByWorkspaceIdAndUserId(UUID workspaceId, String userId);
}
