package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.WorkspaceMemberEntity;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceMemberRepository;
import io.github.cassiocintra.users_management.domain.WorkspaceMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WorkspaceMemberPersistenceAdapter implements WorkspaceMemberRepository {

    private final WorkspaceMemberJpaRepository jpaRepository;

    public WorkspaceMemberPersistenceAdapter(WorkspaceMemberJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<WorkspaceMember> findAll() {
        return jpaRepository.findAll().stream().map(WorkspaceMemberEntity::toDomain).toList();
    }

    @Override
    public Optional<WorkspaceMember> findByUserId(String userId) {
        return jpaRepository.findById(userId).map(WorkspaceMemberEntity::toDomain);
    }

    @Override
    public WorkspaceMember save(WorkspaceMember member) {
        return jpaRepository.save(WorkspaceMemberEntity.from(member)).toDomain();
    }

    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteById(userId);
    }
}
