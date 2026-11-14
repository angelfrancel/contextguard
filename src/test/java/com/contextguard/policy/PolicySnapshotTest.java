package com.contextguard.policy;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicySnapshotTest {

    @Test
    void copiesSourceMap() {
        Map<DetectionType, PolicyOutcome> source =
                new EnumMap<>(DetectionType.class);

        source.put(
                DetectionType.EMAIL,
                PolicyOutcome.REDACT
        );

        PolicySnapshot snapshot =
                new PolicySnapshot(1L, source);

        source.put(
                DetectionType.PAYMENT_CARD,
                PolicyOutcome.BLOCK
        );

        assertTrue(
                snapshot.outcomeFor(
                        DetectionType.PAYMENT_CARD
                ).isEmpty()
        );
    }

    @Test
    void exposesImmutableOutcomeMap() {
        PolicySnapshot snapshot =
                new PolicySnapshot(
                        1L,
                        Map.of(
                                DetectionType.EMAIL,
                                PolicyOutcome.REDACT
                        )
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.outcomesByType().put(
                        DetectionType.PAYMENT_CARD,
                        PolicyOutcome.BLOCK
                )
        );
    }

    @Test
    void returnsConfiguredOutcome() {
        PolicySnapshot snapshot =
                new PolicySnapshot(
                        1L,
                        Map.of(
                                DetectionType.EMAIL,
                                PolicyOutcome.REDACT
                        )
                );

        assertEquals(
                PolicyOutcome.REDACT,
                snapshot.outcomeFor(
                        DetectionType.EMAIL
                ).orElseThrow()
        );
    }

    @Test
    void copiesAndExposesImmutableToolOverrides() {
        Map<ToolPolicyKey, PolicyOutcome> overrides = new java.util.HashMap<>();
        ToolPolicyKey key = new ToolPolicyKey("internal_note", DetectionType.EMAIL);
        overrides.put(key, PolicyOutcome.ALLOW);
        PolicySnapshot snapshot = new PolicySnapshot(
                1L, Map.of(DetectionType.EMAIL, PolicyOutcome.REDACT), overrides
        );
        overrides.clear();

        assertEquals(PolicyOutcome.ALLOW,
                snapshot.outcomeFor("internal_note", DetectionType.EMAIL).orElseThrow());
        assertEquals(PolicyOutcome.REDACT,
                snapshot.outcomeFor("echo", DetectionType.EMAIL).orElseThrow());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.outcomesByToolAndType().put(key, PolicyOutcome.BLOCK));
    }
}
