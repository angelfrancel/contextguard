package com.contextguard.policy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.Policy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyOutcomeResolverTest {

    @Test
    void returnsBlockWhenAnyOutcomeIsBlock() {
        PolicyOutcomeResolver resolver = new PolicyOutcomeResolver();
        List<PolicyOutcome> outcomes = List.of(
                PolicyOutcome.ALLOW,
                PolicyOutcome.BLOCK,
                PolicyOutcome.REDACT
        );

        PolicyOutcome result = resolver.resolve(outcomes);

        assertEquals(PolicyOutcome.BLOCK, result);
    }

    @Test
    void returnsAllowWhenAllOutcomesAllow() {
        PolicyOutcomeResolver resolver = new PolicyOutcomeResolver();
        List<PolicyOutcome> outcomes = List.of(
                PolicyOutcome.ALLOW,
                PolicyOutcome.ALLOW
        );

        PolicyOutcome result = resolver.resolve(outcomes);

        assertEquals(PolicyOutcome.ALLOW, result);
    }

    @Test
    void returnsRedactWhenRedactExistsWithoutBlock() {
        PolicyOutcomeResolver resolver = new PolicyOutcomeResolver();
        List<PolicyOutcome> outcomes = List.of(
                PolicyOutcome.ALLOW,
                PolicyOutcome.REDACT,
                PolicyOutcome.ALLOW
        );

        PolicyOutcome result = resolver.resolve(outcomes);

        assertEquals(PolicyOutcome.REDACT, result);
    }

    @Test
    void returnsAllowWhenOutcomesAreEmpty() {
        PolicyOutcomeResolver resolver = new PolicyOutcomeResolver();
        List<PolicyOutcome> outcome = List.of();

        PolicyOutcome result = resolver.resolve(outcome);

        assertEquals(PolicyOutcome.ALLOW, result);

    }

    @Test
    void rejectsNullOutcomes() {
        PolicyOutcomeResolver resolver = new PolicyOutcomeResolver();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resolver.resolve(null)
        );

        assertEquals("outcomes must not be null", exception.getMessage());
    }
}
