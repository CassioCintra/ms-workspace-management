package io.github.cassiocintra.users_management.adapter.out.schema;

import io.github.cassiocintra.users_management.application.port.out.TenantSchemaPort;
import io.github.cassiocintra.users_management.application.TenantContext;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

@Component
public class TenantSchemaAdapter implements TenantSchemaPort {

    private final DataSource dataSource;

    public TenantSchemaAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void provisionWorkspace(UUID workspaceId) {
        String schema = schemaName(workspaceId);
        createSchema(schema);
        runMigrations(schema);
        TenantContext.setTenantId(schema);
    }

    private void createSchema(String schema) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create workspace schema: " + schema, e);
        }
    }

    private void runMigrations(String schema) {
        Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .locations("classpath:db/migration-tenant")
                .load()
                .migrate();
    }

    public static String schemaName(UUID workspaceId) {
        return "ws_" + workspaceId.toString().replace("-", "_");
    }
}
