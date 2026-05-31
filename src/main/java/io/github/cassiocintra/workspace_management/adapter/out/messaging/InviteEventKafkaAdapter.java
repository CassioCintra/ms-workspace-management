package io.github.cassiocintra.workspace_management.adapter.out.messaging;

import io.github.cassiocintra.workspace_management.application.port.out.InviteEventPublisher;
import io.github.cassiocintra.workspace_management.domain.invite.Invite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InviteEventKafkaAdapter implements InviteEventPublisher {

    private final KafkaTemplate<String, UserInvitedEvent> kafkaTemplate;

    @Value("${users.kafka.topics.user-invited}")
    private String topicUserInvited;

    public InviteEventKafkaAdapter(KafkaTemplate<String, UserInvitedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(Invite invite, UUID workspaceId, String invitedBy) {
        kafkaTemplate.send(topicUserInvited, workspaceId.toString(),
                UserInvitedEvent.from(invite, workspaceId, invitedBy));
    }
}
