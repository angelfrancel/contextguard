package com.contextguard.policy;

import com.contextguard.detection.Detection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PolicyEvaluator {

    private final PolicyOutcomeResolver outcomeResolver;

    public PolicyEvaluator(
            PolicyOutcomeResolver outcomeResolver
    ) {
        this.outcomeResolver = Objects.requireNonNull(
                outcomeResolver,
                "outcomeResolver must not be null"
        );
    }

    public PolicyOutcome evaluate(
            List<Detection> detections,
            PolicySnapshot snapshot
    ) {
        Objects.requireNonNull(
                detections,
                "detections must not be null"
        );
        Objects.requireNonNull(
                snapshot,
                "snapshot must not be null"
        );

        List<PolicyOutcome> outcomes = new ArrayList<>(detections.size());

        for (Detection detection : detections) {
            Objects.requireNonNull(detection, "detection must not be null");

            PolicyOutcome outcome = snapshot.outcomeFor(detection.type()).orElse(PolicyOutcome.BLOCK);

            outcomes.add(outcome);
        }

        return outcomeResolver.resolve(outcomes);
    }
}