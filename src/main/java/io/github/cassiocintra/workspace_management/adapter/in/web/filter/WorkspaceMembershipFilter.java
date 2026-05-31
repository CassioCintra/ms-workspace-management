package io.github.cassiocintra.workspace_management.adapter.in.web.filter;

import io.github.cassiocintra.workspace_management.application.TenantContext;
import io.github.cassiocintra.workspace_management.application.port.in.CheckWorkspaceMembershipUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class WorkspaceMembershipFilter extends OncePerRequestFilter {

    private static final Pattern WORKSPACE_PATH = Pattern.compile("^/workspaces/([0-9a-f\\-]{36})/.*");

    private final CheckWorkspaceMembershipUseCase checkMembership;

    public WorkspaceMembershipFilter(CheckWorkspaceMembershipUseCase checkMembership) {
        this.checkMembership = checkMembership;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        Matcher matcher = WORKSPACE_PATH.matcher(path);

        if (matcher.matches()) {
            UUID workspaceId = UUID.fromString(matcher.group(1));
            String userId = TenantContext.getUserId();
            if (userId == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) userId = auth.getName();
            }

            if (userId == null || !checkMembership.isMember(workspaceId, userId)) {
                log.warn("Membership check failed [workspaceId={}, userId={}]", workspaceId, userId);
                response.sendError(HttpStatus.FORBIDDEN.value(), "Not a member of this workspace");
                return;
            }

            TenantContext.setWorkspaceId(workspaceId.toString());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return HttpMethod.POST.matches(method) && "/workspaces".equals(path);
    }
}
