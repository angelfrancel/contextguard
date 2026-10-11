package com.contextguard.policy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PolicySnapshotCompiler {

    public PolicySnapshot compile(
            long version,
            List<PolicyRule> rules
    ) {
        Objects.requireNonNull(
                rules,
                "rules must not be null"
        );

        Map<DetectionType, PolicyOutcome> compiled =
                new EnumMap<>(DetectionType.class);

        for (PolicyRule rule : rules) {
            Objects.requireNonNull(
                    rule,
                    "rule must not be null"
            );

            if (!rule.enabled()) {
                continue;
            }

            PolicyOutcome previous = compiled.putIfAbsent(
                    rule.detectionType(),
                    rule.outcome()
            );

            if (previous != null) {
                throw new IllegalArgumentException(
                        "duplicate enabled rule for " +
                                rule.detectionType()
                );
            }
        }

        return new PolicySnapshot(
                version,
                compiled
        );
    }
}