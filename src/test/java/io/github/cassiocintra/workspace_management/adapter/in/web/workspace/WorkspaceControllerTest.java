package io.github.cassiocintra.workspace_management.adapter.in.web.workspace;

import io.github.cassiocintra.workspace_management.adapter.in.web.config.SecurityConfig;
import io.github.cassiocintra.workspace_management.adapter.in.web.GlobalExceptionHandler;
import io.github.cassiocintra.workspace_management.adapter.in.web.filter.CorrelatorFilter;
import io.github.cassiocintra.workspace_management.adapter.in.web.filter.TenantExtractorFilter;
import io.github.cassiocintra.workspace_management.adapter.in.web.filter.WorkspaceMembershipFilter;
import io.github.cassiocintra.workspace_management.adapter.in.web.request.ChangeRoleRequest;
import io.github.cassiocintra.workspace_management.adapter.in.web.request.CreateWorkspaceRequest;
import io.github.cassiocintra.workspace_management.application.port.in.CheckWorkspaceMembershipUseCase;
import io.github.cassiocintra.workspace_management.application.port.in.WorkspaceUseCase;
import io.github.cassiocintra.workspace_management.domain.workspace.Workspace;
import io.github.cassiocintra.workspace_management.domain.workspace.WorkspaceMember;
import io.github.cassiocintra.workspace_management.domain.workspace.WorkspaceRole;
import io.github.cassiocintra.workspace_management.domain.exception.MemberNotFoundException;
import io.github.cassiocintra.workspace_management.domain.exception.WorkspaceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, CorrelatorFilter.class, TenantExtractorFilter.class, WorkspaceMembershipFilter.class})
class WorkspaceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WorkspaceUseCase workspaceUseCase;

    @MockitoBean
    CheckWorkspaceMembershipUseCase checkMembership;

    @BeforeEach
    void setUp() {
        when(checkMembership.isMember(any(), any())).thenReturn(true);
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Workspace workspace(UUID id) {
        return Workspace.builder()
                .id(id).name("Acme").slug("acme")
                .ownerId("user-1").createdAt(Instant.now()).build();
    }

    private WorkspaceMember member(String userId, WorkspaceRole role) {
        return WorkspaceMember.builder()
                .userId(userId).role(role).joinedAt(Instant.now()).build();
    }

    // ── POST /workspaces ──────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void shouldCreateWorkspaceAndReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        when(workspaceUseCase.createWorkspace(any())).thenReturn(workspace(id));

        mockMvc.perform(post("/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWorkspaceRequest("Acme", "acme"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.slug").value("acme"))
                .andExpect(header().exists(CorrelatorFilter.HEADER));
    }

    // ── GET /workspaces/{id}/members ──────────────────────────────────────────

    @Test
    @WithMockUser
    void shouldListMembersAndReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(workspaceUseCase.listMembers(id)).thenReturn(List.of(
                member("user-1", WorkspaceRole.ADMIN),
                member("user-2", WorkspaceRole.EDITOR)));

        mockMvc.perform(get("/workspaces/{id}/members", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value("user-1"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].role").value("EDITOR"));
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenWorkspaceNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(workspaceUseCase.listMembers(id)).thenThrow(WorkspaceNotFoundException.notFound(id));

        mockMvc.perform(get("/workspaces/{id}/members", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── PATCH /workspaces/{id}/members/{userId}/role ──────────────────────────

    @Test
    @WithMockUser
    void shouldChangeMemberRoleAndReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(workspaceUseCase).changeMemberRole(any());

        mockMvc.perform(patch("/workspaces/{id}/members/{userId}/role", id, "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeRoleRequest(WorkspaceRole.ADMIN))))
                .andExpect(status().isNoContent());

        verify(workspaceUseCase).changeMemberRole(any());
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenMemberNotFoundOnRoleChange() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(MemberNotFoundException.notFound("ghost")).when(workspaceUseCase).changeMemberRole(any());

        mockMvc.perform(patch("/workspaces/{id}/members/{userId}/role", id, "ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeRoleRequest(WorkspaceRole.VIEWER))))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /workspaces/{id}/members/{userId} ──────────────────────────────

    @Test
    @WithMockUser
    void shouldRemoveMemberAndReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(workspaceUseCase).removeMember(any());

        mockMvc.perform(delete("/workspaces/{id}/members/{userId}", id, "user-1"))
                .andExpect(status().isNoContent());

        verify(workspaceUseCase).removeMember(any());
    }
}
