package io.github.cassiocintra.workspace_management.adapter.in.web.config;

import io.github.cassiocintra.workspace_management.adapter.in.web.filter.TenantExtractorFilter;
import io.github.cassiocintra.workspace_management.adapter.in.web.filter.WorkspaceMembershipFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TenantExtractorFilter tenantExtractorFilter;
    private final WorkspaceMembershipFilter workspaceMembershipFilter;

    public SecurityConfig(TenantExtractorFilter tenantExtractorFilter,
                          WorkspaceMembershipFilter workspaceMembershipFilter) {
        this.tenantExtractorFilter = tenantExtractorFilter;
        this.workspaceMembershipFilter = workspaceMembershipFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/invites/*/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs*", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
                .addFilterAfter(tenantExtractorFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(workspaceMembershipFilter, TenantExtractorFilter.class)
                .build();
    }
}
