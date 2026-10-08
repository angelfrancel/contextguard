package com.contextguard.policy;

import java.util.List;
import java.util.Objects;

public final class PolicyOutcomeResolver {

    public PolicyOutcome resolve(List<PolicyOutcome> outcomes) {
        Objects.requireNonNull(outcomes, "outcomes must not be null");

        PolicyOutcome result = PolicyOutcome.ALLOW;

        for (PolicyOutcome outcome : outcomes) {
            if (outcome == PolicyOutcome.BLOCK) {
                return PolicyOutcome.BLOCK;
            }

            if (outcome == PolicyOutcome.REDACT) {
                result = PolicyOutcome.REDACT;
            }
        }

        return result;
    }
}
