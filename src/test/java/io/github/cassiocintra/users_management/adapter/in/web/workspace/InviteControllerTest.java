package io.github.cassiocintra.users_management.adapter.in.web.workspace;

import io.github.cassiocintra.users_management.adapter.in.web.config.SecurityConfig;
import io.github.cassiocintra.users_management.adapter.in.web.GlobalExceptionHandler;
import io.github.cassiocintra.users_management.adapter.in.web.filter.CorrelatorFilter;
import io.github.cassiocintra.users_management.adapter.in.web.filter.TenantExtractorFilter;
import io.github.cassiocintra.users_management.adapter.in.web.request.CreateInviteRequest;
import io.github.cassiocintra.users_management.application.port.in.InviteUseCase;
import io.github.cassiocintra.users_management.domain.invite.Invite;
import io.github.cassiocintra.users_management.domain.invite.InviteStatus;
import io.github.cassiocintra.users_management.domain.workspace.WorkspaceRole;
import io.github.cassiocintra.users_management.domain.exception.InviteAlreadyPendingException;
import io.github.cassiocintra.users_management.domain.exception.WorkspaceNotFoundException;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InviteController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, CorrelatorFilter.class, TenantExtractorFilter.class})
class InviteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InviteUseCase inviteUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Invite invite() {
        return Invite.builder()
                .id(UUID.randomUUID())
                .email("bob@example.com")
                .role(WorkspaceRole.EDITOR)
                .token(UUID.randomUUID().toString())
                .status(InviteStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(259200))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @WithMockUser
    void shouldCreateInviteAndReturn201() throws Exception {
        when(inviteUseCase.createInvite(any())).thenReturn(invite());

        mockMvc.perform(post("/workspaces/{id}/invites", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateInviteRequest("bob@example.com", WorkspaceRole.EDITOR))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.role").value("EDITOR"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenWorkspaceNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(inviteUseCase.createInvite(any())).thenThrow(new WorkspaceNotFoundException(id));

        mockMvc.perform(post("/workspaces/{id}/invites", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateInviteRequest("bob@example.com", WorkspaceRole.EDITOR))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser
    void shouldReturn409WhenPendingInviteExists() throws Exception {
        when(inviteUseCase.createInvite(any()))
                .thenThrow(new InviteAlreadyPendingException("bob@example.com"));

        mockMvc.perform(post("/workspaces/{id}/invites", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateInviteRequest("bob@example.com", WorkspaceRole.EDITOR))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
