package io.github.cassiocintra.workspace_management.adapter.out.messaging;

import io.github.cassiocintra.workspace_management.domain.invite.Invite;
import io.github.cassiocintra.workspace_management.domain.invite.InviteStatus;
import io.github.cassiocintra.workspace_management.domain.workspace.WorkspaceRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InviteEventKafkaAdapterTest {

    private static final String TOPIC = "user.invited";

    @Mock
    private KafkaTemplate<String, UserInvitedEvent> kafkaTemplate;

    @InjectMocks
    private InviteEventKafkaAdapter adapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "topicUserInvited", TOPIC);
    }

    private Invite invite(String email, WorkspaceRole role) {
        return Invite.builder()
                .id(UUID.randomUUID())
                .email(email)
                .role(role)
                .token(UUID.randomUUID().toString())
                .status(InviteStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void shouldPublishUserInvitedEventToCorrectTopic() {
        UUID workspaceId = UUID.randomUUID();
        Invite invite = invite("bob@example.com", WorkspaceRole.EDITOR);

        adapter.publish(invite, workspaceId, "user-1");

        verify(kafkaTemplate).send(eq(TOPIC), eq(workspaceId.toString()), any(UserInvitedEvent.class));
    }

    @Test
    void shouldIncludeCorrectDataInEvent() {
        UUID workspaceId = UUID.randomUUID();
        Invite invite = invite("bob@example.com", WorkspaceRole.ADMIN);

        adapter.publish(invite, workspaceId, "user-1");

        verify(kafkaTemplate).send(
                eq(TOPIC),
                eq(workspaceId.toString()),
                eq(UserInvitedEvent.from(invite, workspaceId, "user-1"))
        );
    }
}
