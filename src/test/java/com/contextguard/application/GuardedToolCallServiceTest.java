package com.contextguard.application;

import com.contextguard.config.GatewayPolicyConfiguration;
import com.contextguard.persistence.PolicyAuditWriter;
import com.contextguard.policy.DetectionType;
import com.contextguard.policy.PolicyOutcome;
import com.contextguard.policy.PolicySnapshot;
import com.contextguard.policy.PolicySnapshotStore;
import com.contextguard.upstream.UpstreamMcpClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GuardedToolCallServiceTest {

    private final GatewayPolicyConfiguration configuration = new GatewayPolicyConfiguration();
    private final PolicyAuditWriter auditWriter = mock(PolicyAuditWriter.class);
    private final UpstreamMcpClient upstreamClient = mock(UpstreamMcpClient.class);
    private final CallToolResult upstreamResult = CallToolResult.builder()
            .addTextContent("upstream response")
            .build();

    @Test
    void forwardsPlainTextAfterAllowDecisionAndAudit() {
        GuardedToolCallService service = service(emailRedactionSnapshot());
        when(upstreamClient.callTool(eq("echo"), anyMap())).thenReturn(upstreamResult);

        CallToolResult result = service.callTool("echo", Map.of("text", "hello"));

        assertEquals(upstreamResult, result);
        verify(auditWriter).record("echo", PolicyOutcome.ALLOW, 0, 1L);
        verify(upstreamClient).callTool("echo", Map.of("text", "hello"));
    }

    @Test
    void redactsEmailBeforeForwarding() {
        GuardedToolCallService service = service(emailRedactionSnapshot());
        when(upstreamClient.callTool(eq("echo"), anyMap())).thenReturn(upstreamResult);

        CallToolResult result = service.callTool("echo", Map.of("text", "contact a@b.com"));

        assertEquals(upstreamResult, result);
        verify(auditWriter).record("echo", PolicyOutcome.REDACT, 1, 1L);
        verify(upstreamClient).callTool("echo", Map.of("text", "contact [REDACTED]"));
    }

    @Test
    void redactsCardNumberForAnotherDiscoveredTool() {
        GuardedToolCallService service = service(new PolicySnapshot(1L, Map.of(
                DetectionType.PAYMENT_CARD, PolicyOutcome.REDACT
        )));
        when(upstreamClient.callTool(eq("uppercase"), anyMap())).thenReturn(upstreamResult);

        CallToolResult result = service.callTool(
                "uppercase", Map.of("text", "card 4111 1111 1111 1111")
        );

        assertEquals(upstreamResult, result);
        verify(auditWriter).record("uppercase", PolicyOutcome.REDACT, 1, 1L);
        verify(upstreamClient).callTool("uppercase", Map.of("text", "card [REDACTED]"));
    }

    @Test
    void blocksDetectionWithoutAnExplicitRule() {
        GuardedToolCallService service = service(new PolicySnapshot(1L, Map.of()));

        CallToolResult result = service.callTool("echo", Map.of("text", "a@b.com"));

        assertTrue(result.isError());
        verify(auditWriter).record("echo", PolicyOutcome.BLOCK, 1, 1L);
        verifyNoInteractions(upstreamClient);
    }

    @Test
    void doesNotForwardIfAuditCannotBeRecorded() {
        GuardedToolCallService service = service(emailRedactionSnapshot());
        doThrow(new IllegalStateException("database unavailable"))
                .when(auditWriter).record("echo", PolicyOutcome.ALLOW, 0, 1L);

        CallToolResult result = service.callTool("echo", Map.of("text", "hello"));

        assertTrue(result.isError());
        verifyNoInteractions(upstreamClient);
    }

    private GuardedToolCallService service(PolicySnapshot snapshot) {
        return new GuardedToolCallService(
                configuration.detectionPipeline(),
                configuration.policyEvaluator(),
                new PolicySnapshotStore(snapshot),
                configuration.argumentRedactor(),
                auditWriter,
                upstreamClient
        );
    }

    private PolicySnapshot emailRedactionSnapshot() {
        return new PolicySnapshot(1L, Map.of(DetectionType.EMAIL, PolicyOutcome.REDACT));
    }
}
