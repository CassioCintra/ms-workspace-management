package io.github.cassiocintra.users_management.adapter.out.persistence.invite;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.InviteTokenEntity;
import io.github.cassiocintra.users_management.application.port.out.InviteTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class InviteTokenPersistenceAdapter implements InviteTokenRepository {

    private final InviteTokenJpaRepository jpaRepository;

    public InviteTokenPersistenceAdapter(InviteTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(UUID workspaceId, String token) {
        jpaRepository.save(InviteTokenEntity.builder()
                .token(token)
                .workspaceId(workspaceId)
                .build());
    }

    @Override
    public Optional<UUID> findWorkspaceIdByToken(String token) {
        return jpaRepository.findById(token).map(InviteTokenEntity::getWorkspaceId);
    }

    @Override
    public void deleteByToken(String token) {
        jpaRepository.deleteById(token);
    }
}
