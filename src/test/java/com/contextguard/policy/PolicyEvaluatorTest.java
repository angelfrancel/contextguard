package com.contextguard.policy;

import com.contextguard.detection.Detection;
import com.contextguard.detection.TextRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyEvaluatorTest {

    private final PolicyEvaluator evaluator =
            new PolicyEvaluator(new PolicyOutcomeResolver());

    @Test
    void returnsAllowWhenThereAreNoDetections() {
        PolicySnapshot snapshot = new PolicySnapshot(
                1L,
                Map.of()
        );

        PolicyDecision result = evaluator.evaluate(
                List.of(),
                snapshot
        );

        assertEquals(PolicyOutcome.ALLOW, result.outcome());
    }

    @Test
    void returnsConfiguredOutcomeForDetectedType() {
        PolicySnapshot snapshot = new PolicySnapshot(
                1L,
                Map.of(
                        DetectionType.EMAIL,
                        PolicyOutcome.REDACT
                )
        );

        PolicyDecision result = evaluator.evaluate(
                List.of(detection(DetectionType.EMAIL)),
                snapshot
        );

        assertEquals(PolicyOutcome.REDACT, result.outcome());
    }

    @Test
    void returnsBlockWhenDetectedTypeHasNoRule() {
        PolicySnapshot snapshot = new PolicySnapshot(
                1L,
                Map.of(
                        DetectionType.EMAIL,
                        PolicyOutcome.REDACT
                )
        );

        PolicyDecision result = evaluator.evaluate(
                List.of(detection(DetectionType.PAYMENT_CARD)),
                snapshot
        );

        assertEquals(PolicyOutcome.BLOCK, result.outcome());
    }

    @Test
    void blockTakesPrecedenceAcrossMultipleDetections() {
        PolicySnapshot snapshot = new PolicySnapshot(
                1L,
                Map.of(
                        DetectionType.EMAIL,
                        PolicyOutcome.REDACT,
                        DetectionType.PAYMENT_CARD,
                        PolicyOutcome.BLOCK
                )
        );

        PolicyDecision result = evaluator.evaluate(
                List.of(
                        detection(DetectionType.EMAIL),
                        detection(DetectionType.PAYMENT_CARD)
                ),
                snapshot
        );

        assertEquals(PolicyOutcome.BLOCK, result.outcome());
        assertEquals(List.of(), result.detectionsToRedact());
    }

    @Test
    void redactsOnlyDetectionsWithRedactPolicy() {
        Detection email = detection(DetectionType.EMAIL);
        Detection token = detection(DetectionType.AUTHENTICATION_TOKEN);

        PolicySnapshot snapshot = new PolicySnapshot(
                1L,
                Map.of(
                        DetectionType.EMAIL, PolicyOutcome.ALLOW,
                        DetectionType.AUTHENTICATION_TOKEN, PolicyOutcome.REDACT
                )
        );

        PolicyDecision result = evaluator.evaluate(
                List.of(email, token),
                snapshot
        );

        assertEquals(PolicyOutcome.REDACT, result.outcome());
        assertEquals(List.of(token), result.detectionsToRedact());
    }

    @Test
    void rejectsNullDetectionList() {
        PolicySnapshot snapshot = new PolicySnapshot(
                1L,
                Map.of()
        );

        assertThrows(
                NullPointerException.class,
                () -> evaluator.evaluate(null, snapshot)
        );
    }

    @Test
    void rejectsNullSnapshot() {
        assertThrows(
                NullPointerException.class,
                () -> evaluator.evaluate(List.of(), null)
        );
    }

    @Test
    void rejectsNullDetectionInsideList() {
        PolicySnapshot snapshot = new PolicySnapshot(
                1L,
                Map.of()
        );

        List<Detection> detections =
                new java.util.ArrayList<>();
        detections.add(null);

        assertThrows(
                NullPointerException.class,
                () -> evaluator.evaluate(detections, snapshot)
        );
    }

    private Detection detection(DetectionType type) {
        return new Detection(
                type,
                "$.message",
                Optional.of(new TextRange(0, 1))
        );
    }
}