package io.github.cassiocintra.workspace_management.adapter.out.messaging;

import io.github.cassiocintra.workspace_management.domain.token.ApiToken;
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
class TokenEventKafkaAdapterTest {

    private static final String TOPIC = "token.revoked";

    @Mock
    private KafkaTemplate<String, TokenRevokedEvent> kafkaTemplate;

    @InjectMocks
    private TokenEventKafkaAdapter adapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "topicTokenRevoked", TOPIC);
    }

    private ApiToken revokedToken(UUID id) {
        return ApiToken.builder()
                .id(id).name("ci-token").tokenHash("hash-abc")
                .revokedAt(Instant.now()).createdAt(Instant.now()).build();
    }

    @Test
    void shouldPublishTokenRevokedEventToCorrectTopic() {
        UUID workspaceId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        adapter.publish(revokedToken(tokenId), workspaceId, "user-1");

        verify(kafkaTemplate).send(eq(TOPIC), eq(workspaceId.toString()), any(TokenRevokedEvent.class));
    }

    @Test
    void shouldIncludeCorrectDataInEvent() {
        UUID workspaceId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        ApiToken token = revokedToken(tokenId);

        adapter.publish(token, workspaceId, "user-1");

        verify(kafkaTemplate).send(
                eq(TOPIC),
                eq(workspaceId.toString()),
                eq(TokenRevokedEvent.from(token, workspaceId, "user-1"))
        );
    }
}
