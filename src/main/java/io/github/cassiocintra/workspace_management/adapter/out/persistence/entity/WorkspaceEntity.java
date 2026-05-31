package io.github.cassiocintra.workspace_management.adapter.out.persistence.entity;

import io.github.cassiocintra.workspace_management.domain.workspace.Workspace;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workspaces", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static WorkspaceEntity from(Workspace workspace) {
        return WorkspaceEntity.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .slug(workspace.getSlug())
                .ownerId(workspace.getOwnerId())
                .createdAt(workspace.getCreatedAt())
                .build();
    }

    public Workspace toDomain() {
        return Workspace.builder()
                .id(id)
                .name(name)
                .slug(slug)
                .ownerId(ownerId)
                .createdAt(createdAt)
                .build();
    }
}
