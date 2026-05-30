package io.github.cassiocintra.users_management.adapter.in.web.token;

import io.github.cassiocintra.users_management.adapter.in.web.request.CreateApiTokenRequest;
import io.github.cassiocintra.users_management.adapter.in.web.response.ApiTokenResponse;
import io.github.cassiocintra.users_management.application.TenantContext;
import io.github.cassiocintra.users_management.application.port.in.ApiTokenUseCase;
import io.github.cassiocintra.users_management.application.port.in.ApiTokenUseCase.CreatedTokenResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workspaces/{workspaceId}/tokens")
public class ApiTokenController {

    private final ApiTokenUseCase apiTokenUseCase;

    public ApiTokenController(ApiTokenUseCase apiTokenUseCase) {
        this.apiTokenUseCase = apiTokenUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiTokenResponse createToken(@PathVariable UUID workspaceId,
                                        @Valid @RequestBody CreateApiTokenRequest request) {
        TenantContext.setWorkspaceId(workspaceId.toString());
        CreatedTokenResult result = apiTokenUseCase.createToken(
                new ApiTokenUseCase.CreateTokenCommand(request.name()));
        return ApiTokenResponse.fromCreated(result.token(), result.plainToken());
    }

    @GetMapping
    public List<ApiTokenResponse> listTokens(@PathVariable UUID workspaceId) {
        TenantContext.setWorkspaceId(workspaceId.toString());
        return apiTokenUseCase.listTokens().stream()
                .map(ApiTokenResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeToken(@PathVariable UUID workspaceId, @PathVariable UUID id) {
        TenantContext.setWorkspaceId(workspaceId.toString());
        apiTokenUseCase.revokeToken(id);
    }
}
