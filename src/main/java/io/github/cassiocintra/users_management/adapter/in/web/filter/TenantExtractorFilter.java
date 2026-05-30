package io.github.cassiocintra.users_management.adapter.in.web.filter;

import io.github.cassiocintra.users_management.application.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantExtractorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                String workspaceId = jwtAuth.getToken().getClaimAsString("workspace_id");
                String userId = jwtAuth.getToken().getSubject();
                if (workspaceId != null) {
                    TenantContext.setWorkspaceId(workspaceId);
                }
                if (userId != null) {
                    TenantContext.setUserId(userId);
                }
                String email = jwtAuth.getToken().getClaimAsString("email");
                String name = jwtAuth.getToken().getClaimAsString("name");
                if (email != null) TenantContext.setUserEmail(email);
                if (name != null) TenantContext.setUserName(name);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
