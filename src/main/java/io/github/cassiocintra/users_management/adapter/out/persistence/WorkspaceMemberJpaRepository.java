package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.WorkspaceMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberJpaRepository extends JpaRepository<WorkspaceMemberEntity, String> {}
