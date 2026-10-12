package com.contextguard.policy;

import com.contextguard.detection.Detection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PolicyEvaluator {

    private final PolicyOutcomeResolver outcomeResolver;

    public PolicyEvaluator(PolicyOutcomeResolver outcomeResolver) {
        this.outcomeResolver = Objects.requireNonNull(
                outcomeResolver,
                "outcomeResolver must not be null"
        );
    }

    public PolicyDecision evaluate(
            List<Detection> detections,
            PolicySnapshot snapshot
    ) {
        Objects.requireNonNull(detections, "detections must not be null");
        Objects.requireNonNull(snapshot, "snapshot must not be null");

        List<PolicyOutcome> outcomes = new ArrayList<>(detections.size());
        List<Detection> detectionsToRedact = new ArrayList<>();

        for (Detection detection : detections) {
            Objects.requireNonNull(detection, "detection must not be null");

            PolicyOutcome outcome = snapshot.outcomeFor(detection.type())
                    .orElse(PolicyOutcome.BLOCK);

            outcomes.add(outcome);

            if (outcome == PolicyOutcome.REDACT) {
                detectionsToRedact.add(detection);
            }
        }

        PolicyOutcome overallOutcome = outcomeResolver.resolve(outcomes);

        if (overallOutcome == PolicyOutcome.BLOCK) {
            return new PolicyDecision(PolicyOutcome.BLOCK, List.of());
        }

        return new PolicyDecision(overallOutcome, detectionsToRedact);
    }
}