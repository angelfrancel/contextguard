package com.contextguard.persistence;

import com.contextguard.policy.PolicyOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "policy_audit_events")
public class PolicyAuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "tool_name", nullable = false, length = 128)
    private String toolName;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 16)
    private PolicyOutcome outcome;

    @Column(name = "detection_count", nullable = false)
    private int detectionCount;

    @Column(name = "snapshot_version", nullable = false)
    private long snapshotVersion;

    protected PolicyAuditEventEntity() {
    }

    PolicyAuditEventEntity(
            Instant occurredAt,
            String toolName,
            PolicyOutcome outcome,
            int detectionCount,
            long snapshotVersion
    ) {
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        this.toolName = Objects.requireNonNull(toolName, "toolName must not be null");
        this.outcome = Objects.requireNonNull(outcome, "outcome must not be null");
        if (detectionCount < 0) {
            throw new IllegalArgumentException("detectionCount must not be negative");
        }
        if (snapshotVersion <= 0) {
            throw new IllegalArgumentException("snapshotVersion must be positive");
        }
        this.detectionCount = detectionCount;
        this.snapshotVersion = snapshotVersion;
    }

    public Long getId() {
        return id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getToolName() {
        return toolName;
    }

    public PolicyOutcome getOutcome() {
        return outcome;
    }

    public int getDetectionCount() {
        return detectionCount;
    }

    public long getSnapshotVersion() {
        return snapshotVersion;
    }
}
