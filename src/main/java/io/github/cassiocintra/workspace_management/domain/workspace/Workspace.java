package io.github.cassiocintra.workspace_management.domain.workspace;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class Workspace {

    private UUID id;
    private String name;
    private String slug;
    private String ownerId;
    private Instant createdAt;
}
