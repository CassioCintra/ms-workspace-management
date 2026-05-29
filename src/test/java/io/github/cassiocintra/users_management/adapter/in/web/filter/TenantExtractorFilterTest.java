package io.github.cassiocintra.users_management.adapter.in.web.filter;

import io.github.cassiocintra.users_management.application.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TenantExtractorFilterTest {

    private TenantExtractorFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TenantExtractorFilter();
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetTenantAndUserDuringRequest() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-456")
                .claim("workspace_id", "550e8400-e29b-41d4-a716-446655440000")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        AtomicReference<String> capturedTenant = new AtomicReference<>();
        AtomicReference<String> capturedUser = new AtomicReference<>();
        AtomicReference<String> capturedWorkspaceId = new AtomicReference<>();

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (req, res) -> {
                    capturedTenant.set(TenantContext.getTenantId());
                    capturedUser.set(TenantContext.getUserId());
                    capturedWorkspaceId.set(TenantContext.getWorkspaceId());
                }
        );

        assertThat(capturedTenant.get()).isEqualTo("ws_550e8400_e29b_41d4_a716_446655440000");
        assertThat(capturedUser.get()).isEqualTo("user-456");
        assertThat(capturedWorkspaceId.get()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void shouldClearContextAfterRequest() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-456")
                .claim("workspace_id", "ws-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (req, res) -> {}
        );

        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.getUserId()).isNull();
    }

    @Test
    void shouldNotSetTenantWhenNoAuthentication() throws Exception {
        AtomicReference<String> capturedTenant = new AtomicReference<>();

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (req, res) -> capturedTenant.set(TenantContext.getTenantId())
        );

        assertThat(capturedTenant.get()).isNull();
    }

    @Test
    void shouldNotSetTenantWhenWorkspaceClaimAbsent() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-456")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        AtomicReference<String> capturedTenant = new AtomicReference<>();

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (req, res) -> capturedTenant.set(TenantContext.getTenantId())
        );

        assertThat(capturedTenant.get()).isNull();
    }
}
