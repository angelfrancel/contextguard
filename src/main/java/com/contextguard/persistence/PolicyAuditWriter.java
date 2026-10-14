package com.contextguard.persistence;

import com.contextguard.policy.PolicyOutcome;
import com.contextguard.policy.ToolNamePolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
public class PolicyAuditWriter {

    private final PolicyAuditRepository repository;

    public PolicyAuditWriter(PolicyAuditRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Transactional
    public void record(
            String toolName,
            PolicyOutcome outcome,
            int detectionCount,
            long snapshotVersion
    ) {
        ToolNamePolicy.requireValid(toolName);
        repository.save(new PolicyAuditEventEntity(
                Instant.now(), toolName, outcome, detectionCount, snapshotVersion
        ));
    }
}
