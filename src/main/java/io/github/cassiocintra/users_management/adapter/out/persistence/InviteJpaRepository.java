package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.InviteEntity;
import io.github.cassiocintra.users_management.domain.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InviteJpaRepository extends JpaRepository<InviteEntity, UUID> {

    Optional<InviteEntity> findByToken(String token);

    boolean existsByWorkspaceIdAndEmailAndStatus(UUID workspaceId, String email, InviteStatus status);
}
