package io.github.cassiocintra.users_management.application.service;

import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.application.port.in.AcceptInviteUseCase;
import io.github.cassiocintra.users_management.application.port.out.InviteRepository;
import io.github.cassiocintra.users_management.application.port.out.InviteTokenRepository;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceMemberRepository;
import io.github.cassiocintra.users_management.domain.invite.Invite;
import io.github.cassiocintra.users_management.domain.invite.InviteStatus;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.exception.InviteEmailMismatchException;
import io.github.cassiocintra.users_management.domain.exception.InviteExpiredException;
import io.github.cassiocintra.users_management.domain.exception.InviteTokenNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
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
        log.info("Accepting invite [token={}, userId={}, userEmail={}]",
                command.token(), TenantContext.getUserId(), TenantContext.getUserEmail());

        UUID workspaceId = inviteTokenRepository.findWorkspaceIdByToken(command.token())
                .orElseThrow(() -> {
                    log.warn("Invite token not found or already used [token={}]", command.token());
                    return InviteTokenNotFoundException.notFound(command.token());
                });

        TenantContext.setWorkspaceId(workspaceId.toString());
        log.debug("Workspace resolved from token [workspaceId={}]", workspaceId);

        Invite invite = inviteRepository.findByToken(command.token())
                .orElseThrow(() -> InviteTokenNotFoundException.notFound(command.token()));

        if (invite.getStatus() != InviteStatus.PENDING) {
            log.warn("Invite already used or expired [token={}, status={}]", command.token(), invite.getStatus());
            throw InviteTokenNotFoundException.notFound(command.token());
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Invite expired [token={}, expiredAt={}]", command.token(), invite.getExpiresAt());
            throw InviteExpiredException.expired(command.token());
        }

        String jwtEmail = TenantContext.getUserEmail();
        if (jwtEmail != null && !jwtEmail.equalsIgnoreCase(invite.getEmail())) {
            log.warn("Email mismatch on invite accept [jwtEmail={}, inviteEmail={}]", jwtEmail, invite.getEmail());
            throw InviteEmailMismatchException.mismatch();
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

        log.info("Invite accepted — member added [userId={}, workspaceId={}, role={}]",
                TenantContext.getUserId(), workspaceId, invite.getRole());
    }
}
