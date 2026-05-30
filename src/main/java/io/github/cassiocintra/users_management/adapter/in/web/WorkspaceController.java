package io.github.cassiocintra.users_management.adapter.in.web;

import io.github.cassiocintra.users_management.adapter.in.web.request.ChangeRoleRequest;
import io.github.cassiocintra.users_management.adapter.in.web.request.CreateWorkspaceRequest;
import io.github.cassiocintra.users_management.adapter.in.web.response.WorkspaceMemberResponse;
import io.github.cassiocintra.users_management.adapter.in.web.response.WorkspaceResponse;
import io.github.cassiocintra.users_management.application.port.in.WorkspaceUseCase;
import io.github.cassiocintra.users_management.application.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceUseCase workspaceUseCase;

    public WorkspaceController(WorkspaceUseCase workspaceUseCase) {
        this.workspaceUseCase = workspaceUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        return WorkspaceResponse.from(workspaceUseCase.createWorkspace(
                new WorkspaceUseCase.CreateWorkspaceCommand(
                        request.name(), request.slug(),
                        TenantContext.getUserId(),
                        TenantContext.getUserEmail(),
                        TenantContext.getUserName())));
    }

    @GetMapping("/{id}/members")
    public List<WorkspaceMemberResponse> listMembers(@PathVariable UUID id) {
        TenantContext.setWorkspaceId(id.toString());
        return workspaceUseCase.listMembers(id).stream()
                .map(WorkspaceMemberResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/members/{userId}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMemberRole(@PathVariable UUID id,
                                 @PathVariable String userId,
                                 @Valid @RequestBody ChangeRoleRequest request) {
        TenantContext.setWorkspaceId(id.toString());
        workspaceUseCase.changeMemberRole(
                new WorkspaceUseCase.ChangeMemberRoleCommand(id, userId, request.role()));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID id, @PathVariable String userId) {
        TenantContext.setWorkspaceId(id.toString());
        workspaceUseCase.removeMember(new WorkspaceUseCase.RemoveMemberCommand(id, userId));
    }
}
