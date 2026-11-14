package com.contextguard.policy;

import java.util.Objects;

public record PolicyRule(
        long id,
        String toolName,
        DetectionType detectionType,
        PolicyOutcome outcome,
        boolean enabled
) {
    public PolicyRule(long id, DetectionType detectionType, PolicyOutcome outcome, boolean enabled) {
        this(id, null, detectionType, outcome, enabled);
    }

    public PolicyRule {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be greater than zero");
        }

        Objects.requireNonNull(detectionType, "detectionType must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        if (toolName != null) {
            ToolNamePolicy.requireValid(toolName);
        }
    }
}
