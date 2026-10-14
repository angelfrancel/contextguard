package com.contextguard.application;

import com.contextguard.detection.Detection;
import com.contextguard.detection.DetectionPipeline;
import com.contextguard.persistence.PolicyAuditWriter;
import com.contextguard.policy.PolicyDecision;
import com.contextguard.policy.PolicyEvaluator;
import com.contextguard.policy.PolicyOutcome;
import com.contextguard.policy.PolicySnapshot;
import com.contextguard.policy.PolicySnapshotStore;
import com.contextguard.redaction.ArgumentRedactor;
import com.contextguard.upstream.UpstreamMcpClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class GuardedToolCallService {

    private final DetectionPipeline detectionPipeline;
    private final PolicyEvaluator policyEvaluator;
    private final PolicySnapshotStore snapshotStore;
    private final ArgumentRedactor argumentRedactor;
    private final PolicyAuditWriter auditWriter;
    private final UpstreamMcpClient upstreamClient;

    public GuardedToolCallService(
            DetectionPipeline detectionPipeline,
            PolicyEvaluator policyEvaluator,
            PolicySnapshotStore snapshotStore,
            ArgumentRedactor argumentRedactor,
            PolicyAuditWriter auditWriter,
            UpstreamMcpClient upstreamClient
    ) {
        this.detectionPipeline = Objects.requireNonNull(detectionPipeline);
        this.policyEvaluator = Objects.requireNonNull(policyEvaluator);
        this.snapshotStore = Objects.requireNonNull(snapshotStore);
        this.argumentRedactor = Objects.requireNonNull(argumentRedactor);
        this.auditWriter = Objects.requireNonNull(auditWriter);
        this.upstreamClient = Objects.requireNonNull(upstreamClient);
    }

    public CallToolResult callTool(String toolName, Map<String, Object> arguments) {
        try {
            Objects.requireNonNull(toolName, "toolName must not be null");
            Objects.requireNonNull(arguments, "arguments must not be null");

            PolicySnapshot snapshot = snapshotStore.current();
            List<Detection> detections = detectionPipeline.detect(arguments);
            PolicyDecision decision = policyEvaluator.evaluate(detections, snapshot);

            Map<String, Object> safeArguments = null;
            if (decision.outcome() != PolicyOutcome.BLOCK) {
                safeArguments = argumentRedactor.redact(
                        arguments,
                        decision.detectionsToRedact()
                );
            }

            // Commit the decision record before any arguments can leave the gateway.
            auditWriter.record(
                    toolName,
                    decision.outcome(),
                    detections.size(),
                    snapshot.version()
            );

            if (decision.outcome() == PolicyOutcome.BLOCK) {
                return errorResult("Blocked by ContextGuard policy.");
            }

            return upstreamClient.callTool(toolName, safeArguments);
        } catch (RuntimeException exception) {
            return errorResult("ContextGuard could not safely process this tool call.");
        }
    }

    private CallToolResult errorResult(String message) {
        return CallToolResult.builder()
                .addTextContent(message)
                .isError(true)
                .build();
    }
}
