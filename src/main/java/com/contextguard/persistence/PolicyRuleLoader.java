package com.contextguard.persistence;

import com.contextguard.policy.PolicyRule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PolicyRuleLoader {

    private final PolicyRuleRepository repository;

    public PolicyRuleLoader(PolicyRuleRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Transactional(readOnly = true)
    public List<PolicyRule> loadAll() {
        List<PolicyRule> rules = new ArrayList<>();

        for (PolicyRuleEntity entity : repository.findAll()) {
            rules.add(new PolicyRule(
                    Objects.requireNonNull(entity.getId()),
                    entity.getDetectionType(),
                    entity.getOutcome(),
                    entity.isEnabled()
            ));
        }

        return List.copyOf(rules);
    }
}