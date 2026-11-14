package com.contextguard.policy;

import java.util.Objects;

public record ToolPolicyKey(String toolName, DetectionType detectionType) {
    public ToolPolicyKey {
        ToolNamePolicy.requireValid(toolName);
        Objects.requireNonNull(detectionType, "detectionType must not be null");
    }
}
