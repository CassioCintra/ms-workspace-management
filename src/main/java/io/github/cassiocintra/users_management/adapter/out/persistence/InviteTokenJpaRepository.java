package io.github.cassiocintra.users_management.adapter.out.persistence;

import io.github.cassiocintra.users_management.adapter.out.persistence.entity.InviteTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteTokenJpaRepository extends JpaRepository<InviteTokenEntity, String> {}
