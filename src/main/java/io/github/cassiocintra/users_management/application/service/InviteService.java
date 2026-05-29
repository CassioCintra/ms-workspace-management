package io.github.cassiocintra.users_management.application.service;

import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.application.port.in.InviteUseCase;
import io.github.cassiocintra.users_management.application.port.out.EmailPort;
import io.github.cassiocintra.users_management.application.port.out.InviteEventPublisher;
import io.github.cassiocintra.users_management.application.port.out.InviteRepository;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceRepository;
import io.github.cassiocintra.users_management.domain.Invite;
import io.github.cassiocintra.users_management.domain.InviteStatus;
import io.github.cassiocintra.users_management.domain.Workspace;
import io.github.cassiocintra.users_management.domain.exception.InviteAlreadyPendingException;
import io.github.cassiocintra.users_management.domain.exception.WorkspaceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class InviteService implements InviteUseCase {

    private final InviteRepository inviteRepository;
    private final WorkspaceRepository workspaceRepository;
    private final InviteEventPublisher inviteEventPublisher;
    private final EmailPort emailPort;

    public InviteService(InviteRepository inviteRepository,
                         WorkspaceRepository workspaceRepository,
                         InviteEventPublisher inviteEventPublisher,
                         EmailPort emailPort) {
        this.inviteRepository = inviteRepository;
        this.workspaceRepository = workspaceRepository;
        this.inviteEventPublisher = inviteEventPublisher;
        this.emailPort = emailPort;
    }

    @Override
    public Invite createInvite(CreateInviteCommand command) {
        Workspace workspace = workspaceRepository.findById(command.workspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException(command.workspaceId()));

        if (inviteRepository.existsByEmailAndStatus(command.email(), InviteStatus.PENDING)) {
            throw new InviteAlreadyPendingException(command.email());
        }

        Invite invite = Invite.builder()
                .id(UUID.randomUUID())
                .email(command.email())
                .role(command.role())
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(72, ChronoUnit.HOURS))
                .status(InviteStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        Invite saved = inviteRepository.save(invite);

        emailPort.sendInvite(saved, workspace.getName());
        inviteEventPublisher.publish(saved, command.workspaceId(), TenantContext.getUserId());

        return saved;
    }
}
