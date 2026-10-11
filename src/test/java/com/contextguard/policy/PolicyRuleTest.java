package com.contextguard.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyRuleTest {

    @Test
    void createsEnabledRule() {
        assertDoesNotThrow(
                () -> new PolicyRule(
                        1L,
                        DetectionType.EMAIL,
                        PolicyOutcome.REDACT,
                        true
                )
        );
    }

    @Test
    void createsDisabledRule() {
        assertDoesNotThrow(
                () -> new PolicyRule(
                        2L,
                        DetectionType.EMAIL,
                        PolicyOutcome.REDACT,
                        false
                )
        );
    }

    @Test
    void rejectsNonPositiveId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PolicyRule(
                        0L,
                        DetectionType.EMAIL,
                        PolicyOutcome.REDACT,
                        true
                )
        );
    }

    @Test
    void rejectsNullDetectionType() {
        assertThrows(
                NullPointerException.class,
                () -> new PolicyRule(
                        1L,
                        null,
                        PolicyOutcome.REDACT,
                        true
                )
        );
    }

    @Test
    void rejectsNullOutcome() {
        assertThrows(
                NullPointerException.class,
                () -> new PolicyRule(
                        1L,
                        DetectionType.EMAIL,
                        null,
                        true
                )
        );
    }
}