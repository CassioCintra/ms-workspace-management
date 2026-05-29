package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.InviteEntity;
import io.github.cassiocintra.users_management.application.port.out.InviteRepository;
import io.github.cassiocintra.users_management.domain.Invite;
import io.github.cassiocintra.users_management.domain.InviteStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InvitePersistenceAdapter implements InviteRepository {

    private final InviteJpaRepository jpaRepository;

    public InvitePersistenceAdapter(InviteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Invite save(Invite invite) {
        return jpaRepository.save(InviteEntity.from(invite)).toDomain();
    }

    @Override
    public Optional<Invite> findByToken(String token) {
        return jpaRepository.findByToken(token).map(InviteEntity::toDomain);
    }

    @Override
    public boolean existsByEmailAndStatus(String email, InviteStatus status) {
        return jpaRepository.existsByEmailAndStatus(email, status);
    }
}
