package io.github.cassiocintra.users_management.application.service;

import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.application.port.in.AcceptInviteUseCase;
import io.github.cassiocintra.users_management.application.port.out.InviteRepository;
import io.github.cassiocintra.users_management.application.port.out.InviteTokenRepository;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceMemberRepository;
import io.github.cassiocintra.users_management.domain.Invite;
import io.github.cassiocintra.users_management.domain.InviteStatus;
import io.github.cassiocintra.users_management.domain.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.exception.InviteEmailMismatchException;
import io.github.cassiocintra.users_management.domain.exception.InviteExpiredException;
import io.github.cassiocintra.users_management.domain.exception.InviteTokenNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AcceptInviteService implements AcceptInviteUseCase {

    private final InviteTokenRepository inviteTokenRepository;
    private final InviteRepository inviteRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public AcceptInviteService(InviteTokenRepository inviteTokenRepository,
                               InviteRepository inviteRepository,
                               WorkspaceMemberRepository workspaceMemberRepository) {
        this.inviteTokenRepository = inviteTokenRepository;
        this.inviteRepository = inviteRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Override
    @Transactional
    public void acceptInvite(AcceptInviteCommand command) {
        UUID workspaceId = inviteTokenRepository.findWorkspaceIdByToken(command.token())
                .orElseThrow(() -> new InviteTokenNotFoundException(command.token()));

        TenantContext.setWorkspaceId(workspaceId.toString());

        Invite invite = inviteRepository.findByToken(command.token())
                .orElseThrow(() -> new InviteTokenNotFoundException(command.token()));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new InviteTokenNotFoundException(command.token());
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            throw new InviteExpiredException(command.token());
        }

        String jwtEmail = TenantContext.getUserEmail();
        if (jwtEmail != null && !jwtEmail.equalsIgnoreCase(invite.getEmail())) {
            throw new InviteEmailMismatchException();
        }

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .userId(TenantContext.getUserId())
                .email(jwtEmail)
                .name(TenantContext.getUserName())
                .role(invite.getRole())
                .joinedAt(Instant.now())
                .build());

        inviteRepository.save(invite.accept());
        inviteTokenRepository.deleteByToken(command.token());
    }
}
