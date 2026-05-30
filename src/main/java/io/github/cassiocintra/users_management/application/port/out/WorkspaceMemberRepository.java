package io.github.cassiocintra.users_management.application.port.out;

import io.github.cassiocintra.users_management.domain.workspace.WorkspaceMember;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository {

    List<WorkspaceMember> findAll();

    Optional<WorkspaceMember> findByUserId(String userId);

    WorkspaceMember save(WorkspaceMember member);

    void deleteByUserId(String userId);
}
