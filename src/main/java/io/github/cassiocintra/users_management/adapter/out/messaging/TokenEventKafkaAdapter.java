package io.github.cassiocintra.users_management.adapter.out.messaging;

import io.github.cassiocintra.users_management.application.port.out.TokenEventPublisher;
import io.github.cassiocintra.users_management.domain.ApiToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenEventKafkaAdapter implements TokenEventPublisher {

    private final KafkaTemplate<String, TokenRevokedEvent> kafkaTemplate;

    @Value("${users.kafka.topics.token-revoked}")
    private String topicTokenRevoked;

    public TokenEventKafkaAdapter(KafkaTemplate<String, TokenRevokedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(ApiToken token, UUID workspaceId, String revokedBy) {
        kafkaTemplate.send(topicTokenRevoked, workspaceId != null ? workspaceId.toString() : null,
                TokenRevokedEvent.from(token, workspaceId, revokedBy));
    }
}
