package io.github.cassiocintra.workspace_management.adapter.in.web.invite;

import io.github.cassiocintra.workspace_management.adapter.in.web.request.AcceptInviteRequest;
import io.github.cassiocintra.workspace_management.adapter.in.web.response.InviteInfoResponse;
import io.github.cassiocintra.workspace_management.application.port.in.AcceptInviteUseCase;
import io.github.cassiocintra.workspace_management.application.port.in.InviteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invites")
public class InviteAcceptController {

    private final InviteUseCase inviteUseCase;
    private final AcceptInviteUseCase acceptInviteUseCase;

    public InviteAcceptController(InviteUseCase inviteUseCase, AcceptInviteUseCase acceptInviteUseCase) {
        this.inviteUseCase = inviteUseCase;
        this.acceptInviteUseCase = acceptInviteUseCase;
    }

    @GetMapping("/{token}/info")
    public InviteInfoResponse getInviteInfo(@PathVariable String token) {
        return InviteInfoResponse.from(inviteUseCase.getInviteInfo(token));
    }

    @PostMapping("/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        acceptInviteUseCase.acceptInvite(new AcceptInviteUseCase.AcceptInviteCommand(request.token()));
    }
}
