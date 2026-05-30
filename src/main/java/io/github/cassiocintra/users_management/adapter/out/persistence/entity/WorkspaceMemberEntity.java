package io.github.cassiocintra.users_management.adapter.out.persistence.entity;

import io.github.cassiocintra.users_management.domain.WorkspaceMember;
import io.github.cassiocintra.users_management.domain.WorkspaceRole;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workspace_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMemberEntity {

    @EmbeddedId
    private WorkspaceMemberId id;

    @Column
    private String email;

    @Column
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    public static WorkspaceMemberEntity from(UUID workspaceId, WorkspaceMember member) {
        return WorkspaceMemberEntity.builder()
                .id(new WorkspaceMemberId(workspaceId, member.getUserId()))
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    public WorkspaceMember toDomain() {
        return WorkspaceMember.builder()
                .userId(id.getUserId())
                .email(email)
                .name(name)
                .role(role)
                .joinedAt(joinedAt)
                .build();
    }
}
