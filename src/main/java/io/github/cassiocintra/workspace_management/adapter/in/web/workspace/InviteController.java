package io.github.cassiocintra.workspace_management.adapter.in.web.workspace;

import io.github.cassiocintra.workspace_management.adapter.in.web.request.CreateInviteRequest;
import io.github.cassiocintra.workspace_management.adapter.in.web.response.InviteResponse;
import io.github.cassiocintra.workspace_management.application.port.in.InviteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/workspaces")
public class InviteController {

    private final InviteUseCase inviteUseCase;

    public InviteController(InviteUseCase inviteUseCase) {
        this.inviteUseCase = inviteUseCase;
    }

    @PostMapping("/{id}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(@PathVariable UUID id,
                                       @Valid @RequestBody CreateInviteRequest request) {
        return InviteResponse.from(inviteUseCase.createInvite(
                new InviteUseCase.CreateInviteCommand(id, request.email(), request.role())));
    }
}
