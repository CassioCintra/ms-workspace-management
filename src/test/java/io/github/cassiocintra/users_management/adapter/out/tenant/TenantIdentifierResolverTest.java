package io.github.cassiocintra.users_management.adapter.out.tenant;

import io.github.cassiocintra.users_management.application.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantIdentifierResolverTest {

    private TenantIdentifierResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new TenantIdentifierResolver();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldReturnPublicWhenNoTenantSet() {
        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo("public");
    }

    @Test
    void shouldReturnTenantIdWhenSet() {
        TenantContext.setTenantId("ws_abc123");
        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo("ws_abc123");
    }

    @Test
    void shouldNotValidateExistingCurrentSessions() {
        assertThat(resolver.validateExistingCurrentSessions()).isFalse();
    }
}
