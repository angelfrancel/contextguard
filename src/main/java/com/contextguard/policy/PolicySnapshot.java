package com.contextguard.policy;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record PolicySnapshot(
        long version,
        Map<DetectionType, PolicyOutcome> outcomesByType,
        Map<ToolPolicyKey, PolicyOutcome> outcomesByToolAndType
) {

    public PolicySnapshot(long version, Map<DetectionType, PolicyOutcome> outcomesByType) {
        this(version, outcomesByType, Map.of());
    }

    public PolicySnapshot {
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "version must be greater than zero"
            );
        }

        Objects.requireNonNull(
                outcomesByType,
                "outcomesByType must not be null"
        );

        outcomesByType = Map.copyOf(outcomesByType);
        Objects.requireNonNull(outcomesByToolAndType, "outcomesByToolAndType must not be null");
        outcomesByToolAndType = Map.copyOf(outcomesByToolAndType);
    }

    public Optional<PolicyOutcome> outcomeFor(
            DetectionType detectionType
    ) {
        Objects.requireNonNull(
                detectionType,
                "detectionType must not be null"
        );

        return Optional.ofNullable(
                outcomesByType.get(detectionType)
        );
    }

    public Optional<PolicyOutcome> outcomeFor(String toolName, DetectionType detectionType) {
        ToolPolicyKey key = new ToolPolicyKey(toolName, detectionType);
        PolicyOutcome specific = outcomesByToolAndType.get(key);
        return specific != null ? Optional.of(specific) : outcomeFor(detectionType);
    }
}
