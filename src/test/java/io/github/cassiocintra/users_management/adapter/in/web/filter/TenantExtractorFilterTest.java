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
    void shouldSetWorkspaceIdAndUserDuringRequest() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-456")
                .claim("workspace_id", "550e8400-e29b-41d4-a716-446655440000")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        AtomicReference<String> capturedUser = new AtomicReference<>();
        AtomicReference<String> capturedWorkspaceId = new AtomicReference<>();

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (req, res) -> {
                    capturedUser.set(TenantContext.getUserId());
                    capturedWorkspaceId.set(TenantContext.getWorkspaceId());
                }
        );

        assertThat(capturedUser.get()).isEqualTo("user-456");
        assertThat(capturedWorkspaceId.get()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void shouldClearContextAfterRequest() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-456")
                .claim("workspace_id", "550e8400-e29b-41d4-a716-446655440000")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (req, res) -> {});

        assertThat(TenantContext.getWorkspaceId()).isNull();
        assertThat(TenantContext.getUserId()).isNull();
    }

    @Test
    void shouldNotSetWorkspaceWhenNoAuthentication() throws Exception {
        AtomicReference<String> capturedWorkspaceId = new AtomicReference<>();

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (req, res) -> capturedWorkspaceId.set(TenantContext.getWorkspaceId())
        );

        assertThat(capturedWorkspaceId.get()).isNull();
    }

    @Test
    void shouldNotSetWorkspaceWhenClaimAbsent() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-456")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        AtomicReference<String> capturedWorkspaceId = new AtomicReference<>();

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (req, res) -> capturedWorkspaceId.set(TenantContext.getWorkspaceId())
        );

        assertThat(capturedWorkspaceId.get()).isNull();
    }
}
