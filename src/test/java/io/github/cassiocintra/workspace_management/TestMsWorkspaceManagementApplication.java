package io.github.cassiocintra.workspace_management;

import org.springframework.boot.SpringApplication;

public class TestMsWorkspaceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.from(WorkspaceManagementApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
