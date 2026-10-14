package com.contextguard.persistence;

import com.contextguard.policy.DetectionType;
import com.contextguard.policy.PolicyOutcome;
import jakarta.persistence.*;

@Entity
@Table(name = "policy_rules")
public class PolicyRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "detection_type", nullable = false, length = 64)
    private DetectionType detectionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 16)
    private PolicyOutcome outcome;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected PolicyRuleEntity() {
    }

    public Long getId() {
        return id;
    }

    public DetectionType getDetectionType() {
        return detectionType;
    }

    public PolicyOutcome getOutcome() {
        return outcome;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
