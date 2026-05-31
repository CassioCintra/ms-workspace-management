package io.github.cassiocintra.workspace_management;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@Import(TestcontainersConfiguration.class)
@Slf4j
@SpringBootTest
class MsWorkspaceManagementApplicationTests {

    @Test
    void contextLoads() {
        log.info("Context loaded");
    }
}
