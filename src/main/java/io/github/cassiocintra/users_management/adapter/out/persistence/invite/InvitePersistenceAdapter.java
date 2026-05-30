package io.github.cassiocintra.users_management.adapter.out.persistence.invite;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.InviteEntity;
import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.application.port.out.InviteRepository;
import io.github.cassiocintra.users_management.domain.invite.Invite;
import io.github.cassiocintra.users_management.domain.invite.InviteStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class InvitePersistenceAdapter implements InviteRepository {

    private final InviteJpaRepository jpaRepository;

    public InvitePersistenceAdapter(InviteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Invite save(Invite invite) {
        return jpaRepository.save(InviteEntity.from(workspaceId(), invite)).toDomain();
    }

    @Override
    public Optional<Invite> findByToken(String token) {
        return jpaRepository.findByToken(token).map(InviteEntity::toDomain);
    }

    @Override
    public boolean existsByEmailAndStatus(String email, InviteStatus status) {
        return jpaRepository.existsByWorkspaceIdAndEmailAndStatus(workspaceId(), email, status);
    }

    private UUID workspaceId() {
        return UUID.fromString(TenantContext.getWorkspaceId());
    }
}
