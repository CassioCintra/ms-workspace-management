package io.github.cassiocintra.workspace_management.adapter.in.web.filter;

import io.github.cassiocintra.workspace_management.application.TenantContext;
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
    void shouldSetUserContextDuringRequest() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-456")
                .claim("email", "alice@example.com")
                .claim("name", "Alice")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        AtomicReference<String> capturedUser = new AtomicReference<>();
        AtomicReference<String> capturedEmail = new AtomicReference<>();

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (req, res) -> {
                    capturedUser.set(TenantContext.getUserId());
                    capturedEmail.set(TenantContext.getUserEmail());
                }
        );

        assertThat(capturedUser.get()).isEqualTo("user-456");
        assertThat(capturedEmail.get()).isEqualTo("alice@example.com");
    }

    @Test
    void shouldClearContextAfterRequest() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-456")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (req, res) -> {});

        assertThat(TenantContext.getUserId()).isNull();
        assertThat(TenantContext.getUserEmail()).isNull();
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
    void shouldNotSetEmailOrNameWhenClaimsAbsent() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-456")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        AtomicReference<String> capturedEmail = new AtomicReference<>();

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (req, res) -> capturedEmail.set(TenantContext.getUserEmail())
        );

        assertThat(capturedEmail.get()).isNull();
    }
}
