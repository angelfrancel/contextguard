package com.contextguard.policy;

import com.contextguard.detection.Detection;

import java.util.List;
import java.util.Objects;

public record PolicyDecision(
        PolicyOutcome outcome,
        List<Detection> detectionsToRedact
) {
    public PolicyDecision {
        Objects.requireNonNull(outcome, "outcome must not be null");
        Objects.requireNonNull(detectionsToRedact, "detectionsToRedact list must not be null");

        detectionsToRedact = List.copyOf(detectionsToRedact);
    }
}