package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.ApiTokenEntity;
import io.github.cassiocintra.users_management.application.port.out.ApiTokenRepository;
import io.github.cassiocintra.users_management.domain.ApiToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ApiTokenPersistenceAdapter implements ApiTokenRepository {

    private final ApiTokenJpaRepository jpaRepository;

    public ApiTokenPersistenceAdapter(ApiTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ApiToken save(ApiToken token) {
        return jpaRepository.save(ApiTokenEntity.from(token)).toDomain();
    }

    @Override
    public List<ApiToken> findAll() {
        return jpaRepository.findAll().stream().map(ApiTokenEntity::toDomain).toList();
    }

    @Override
    public Optional<ApiToken> findById(UUID id) {
        return jpaRepository.findById(id).map(ApiTokenEntity::toDomain);
    }
}
