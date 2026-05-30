package io.github.cassiocintra.users_management.adapter.in.web.token;

import io.github.cassiocintra.users_management.adapter.in.web.config.SecurityConfig;
import io.github.cassiocintra.users_management.adapter.in.web.GlobalExceptionHandler;
import io.github.cassiocintra.users_management.adapter.in.web.filter.CorrelatorFilter;
import io.github.cassiocintra.users_management.adapter.in.web.filter.TenantExtractorFilter;
import io.github.cassiocintra.users_management.adapter.in.web.request.CreateApiTokenRequest;
import io.github.cassiocintra.users_management.application.port.in.ApiTokenUseCase;
import io.github.cassiocintra.users_management.application.port.in.ApiTokenUseCase.CreatedTokenResult;
import io.github.cassiocintra.users_management.domain.token.ApiToken;
import io.github.cassiocintra.users_management.domain.exception.ApiTokenAlreadyRevokedException;
import io.github.cassiocintra.users_management.domain.exception.ApiTokenNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiTokenController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, CorrelatorFilter.class, TenantExtractorFilter.class})
class ApiTokenControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ApiTokenUseCase apiTokenUseCase;

    private final UUID workspaceId = UUID.randomUUID();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ApiToken token(UUID id, String name) {
        return ApiToken.builder()
                .id(id).name(name).tokenHash("hash-abc")
                .createdAt(Instant.now()).build();
    }

    @Test
    @WithMockUser
    void shouldCreateTokenAndReturn201WithPlainToken() throws Exception {
        UUID id = UUID.randomUUID();
        ApiToken token = token(id, "ci-token");
        when(apiTokenUseCase.createToken(any())).thenReturn(new CreatedTokenResult(token, "plain-secret-abc"));

        mockMvc.perform(post("/workspaces/{workspaceId}/tokens", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApiTokenRequest("ci-token"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ci-token"))
                .andExpect(jsonPath("$.plainToken").value("plain-secret-abc"))
                .andExpect(jsonPath("$.revokedAt").doesNotExist());
    }

    @Test
    @WithMockUser
    void shouldListTokensAndReturn200() throws Exception {
        when(apiTokenUseCase.listTokens()).thenReturn(List.of(
                token(UUID.randomUUID(), "ci-token"),
                token(UUID.randomUUID(), "deploy-token")));

        mockMvc.perform(get("/workspaces/{workspaceId}/tokens", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("ci-token"))
                .andExpect(jsonPath("$[0].plainToken").doesNotExist());
    }

    @Test
    @WithMockUser
    void shouldRevokeTokenAndReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(apiTokenUseCase).revokeToken(id);

        mockMvc.perform(delete("/workspaces/{workspaceId}/tokens/{id}", workspaceId, id))
                .andExpect(status().isNoContent());

        verify(apiTokenUseCase).revokeToken(id);
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenTokenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ApiTokenNotFoundException(id)).when(apiTokenUseCase).revokeToken(id);

        mockMvc.perform(delete("/workspaces/{workspaceId}/tokens/{id}", workspaceId, id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser
    void shouldReturn409WhenTokenAlreadyRevoked() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ApiTokenAlreadyRevokedException(id)).when(apiTokenUseCase).revokeToken(id);

        mockMvc.perform(delete("/workspaces/{workspaceId}/tokens/{id}", workspaceId, id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
