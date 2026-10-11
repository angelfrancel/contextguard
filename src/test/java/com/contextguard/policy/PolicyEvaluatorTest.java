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

        PolicyOutcome result = evaluator.evaluate(
                List.of(),
                snapshot
        );

        assertEquals(PolicyOutcome.ALLOW, result);
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

        PolicyOutcome result = evaluator.evaluate(
                List.of(detection(DetectionType.EMAIL)),
                snapshot
        );

        assertEquals(PolicyOutcome.REDACT, result);
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

        PolicyOutcome result = evaluator.evaluate(
                List.of(detection(DetectionType.PAYMENT_CARD)),
                snapshot
        );

        assertEquals(PolicyOutcome.BLOCK, result);
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

        PolicyOutcome result = evaluator.evaluate(
                List.of(
                        detection(DetectionType.EMAIL),
                        detection(DetectionType.PAYMENT_CARD)
                ),
                snapshot
        );

        assertEquals(PolicyOutcome.BLOCK, result);
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