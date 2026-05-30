package io.github.cassiocintra.users_management.application.service;

import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.application.port.in.InviteUseCase;
import io.github.cassiocintra.users_management.application.port.out.EmailPort;
import io.github.cassiocintra.users_management.application.port.out.InviteEventPublisher;
import io.github.cassiocintra.users_management.application.port.out.InviteRepository;
import io.github.cassiocintra.users_management.application.port.out.InviteTokenRepository;
import io.github.cassiocintra.users_management.application.port.out.WorkspaceRepository;
import io.github.cassiocintra.users_management.domain.invite.Invite;
import io.github.cassiocintra.users_management.domain.invite.InviteStatus;
import io.github.cassiocintra.users_management.domain.workspace.Workspace;
import io.github.cassiocintra.users_management.domain.exception.InviteAlreadyPendingException;
import io.github.cassiocintra.users_management.domain.exception.InviteTokenNotFoundException;
import io.github.cassiocintra.users_management.domain.exception.WorkspaceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class InviteService implements InviteUseCase {

    private final InviteRepository inviteRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final WorkspaceRepository workspaceRepository;
    private final InviteEventPublisher inviteEventPublisher;
    private final EmailPort emailPort;

    public InviteService(InviteRepository inviteRepository,
                         InviteTokenRepository inviteTokenRepository,
                         WorkspaceRepository workspaceRepository,
                         InviteEventPublisher inviteEventPublisher,
                         EmailPort emailPort) {
        this.inviteRepository = inviteRepository;
        this.inviteTokenRepository = inviteTokenRepository;
        this.workspaceRepository = workspaceRepository;
        this.inviteEventPublisher = inviteEventPublisher;
        this.emailPort = emailPort;
    }

    @Override
    public Invite createInvite(CreateInviteCommand command) {
        log.info("Creating invite [workspaceId={}, email={}, role={}, invitedBy={}]",
                command.workspaceId(), command.email(), command.role(), TenantContext.getUserId());

        Workspace workspace = workspaceRepository.findById(command.workspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException(command.workspaceId()));

        if (inviteRepository.existsByEmailAndStatus(command.email(), InviteStatus.PENDING)) {
            log.warn("Duplicate invite attempt [workspaceId={}, email={}]", command.workspaceId(), command.email());
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
        inviteTokenRepository.save(command.workspaceId(), saved.getToken());
        log.debug("Invite persisted [inviteId={}, token={}]", saved.getId(), saved.getToken());

        emailPort.sendInvite(saved, workspace.getName(), TenantContext.getUserName());
        log.info("Invite email sent [inviteId={}, to={}, workspace={}]",
                saved.getId(), saved.getEmail(), workspace.getName());

        inviteEventPublisher.publish(saved, command.workspaceId(), TenantContext.getUserId());
        log.debug("Invite event published [inviteId={}, workspaceId={}]", saved.getId(), command.workspaceId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public InviteInfo getInviteInfo(String token) {
        log.info("Fetching invite info [token={}]", token);

        UUID workspaceId = inviteTokenRepository.findWorkspaceIdByToken(token)
                .orElseThrow(() -> new InviteTokenNotFoundException(token));

        TenantContext.setWorkspaceId(workspaceId.toString());

        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new InviteTokenNotFoundException(token));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));

        log.info("Invite info retrieved [email={}, workspace={}, role={}, status={}]",
                invite.getEmail(), workspace.getName(), invite.getRole(), invite.getStatus());

        return new InviteInfo(invite.getEmail(), workspace.getName(), invite.getRole(),
                invite.getExpiresAt(), invite.getStatus());
    }
}
