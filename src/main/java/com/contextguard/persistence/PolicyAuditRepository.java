package com.contextguard.persistence;

import org.springframework.data.repository.Repository;

public interface PolicyAuditRepository extends Repository<PolicyAuditEventEntity, Long> {

    PolicyAuditEventEntity save(PolicyAuditEventEntity event);
}
