package com.contextguard.policy;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicySnapshotCompilerTest {

    private final PolicySnapshotCompiler compiler =
            new PolicySnapshotCompiler();

    @Test
    void compilesEnabledRules() {
        List<PolicyRule> rules =
                List.of(
                        new PolicyRule(
                                1L,
                                DetectionType.EMAIL,
                                PolicyOutcome.REDACT,
                                true
                        ),
                        new PolicyRule(
                                2L,
                                DetectionType.PAYMENT_CARD,
                                PolicyOutcome.BLOCK,
                                true
                        )
                );

        PolicySnapshot snapshot =
                compiler.compile(1L, rules);

        assertEquals(
                Optional.of(PolicyOutcome.REDACT),
                snapshot.outcomeFor(DetectionType.EMAIL)
        );
        assertEquals(
                Optional.of(PolicyOutcome.BLOCK),
                snapshot.outcomeFor(
                        DetectionType.PAYMENT_CARD
                )
        );
    }

    @Test
    void ignoresDisabledRules() {
        PolicyRule disabledRule =
                new PolicyRule(
                        1L,
                        DetectionType.EMAIL,
                        PolicyOutcome.REDACT,
                        false
                );

        PolicySnapshot snapshot =
                compiler.compile(
                        1L,
                        List.of(disabledRule)
                );

        assertTrue(
                snapshot.outcomeFor(
                        DetectionType.EMAIL
                ).isEmpty()
        );
    }

    @Test
    void rejectsDuplicateEnabledRules() {
        List<PolicyRule> rules =
                List.of(
                        new PolicyRule(
                                1L,
                                DetectionType.EMAIL,
                                PolicyOutcome.REDACT,
                                true
                        ),
                        new PolicyRule(
                                2L,
                                DetectionType.EMAIL,
                                PolicyOutcome.BLOCK,
                                true
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> compiler.compile(1L, rules)
        );
    }

    @Test
    void allowsDisabledDuplicateRule() {
        List<PolicyRule> rules =
                List.of(
                        new PolicyRule(
                                1L,
                                DetectionType.EMAIL,
                                PolicyOutcome.REDACT,
                                true
                        ),
                        new PolicyRule(
                                2L,
                                DetectionType.EMAIL,
                                PolicyOutcome.BLOCK,
                                false
                        )
                );

        PolicySnapshot snapshot =
                compiler.compile(1L, rules);

        assertEquals(
                Optional.of(PolicyOutcome.REDACT),
                snapshot.outcomeFor(
                        DetectionType.EMAIL
                )
        );
    }

    @Test
    void rejectsNullRuleList() {
        assertThrows(
                NullPointerException.class,
                () -> compiler.compile(1L, null)
        );
    }

    @Test
    void rejectsNullRuleInsideList() {
        List<PolicyRule> rules =
                Collections.singletonList(null);

        assertThrows(
                NullPointerException.class,
                () -> compiler.compile(1L, rules)
        );
    }
}