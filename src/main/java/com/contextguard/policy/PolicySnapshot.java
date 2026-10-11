package com.contextguard.policy;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record PolicySnapshot(
        long version,
        Map<DetectionType, PolicyOutcome> outcomesByType
) {

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
}