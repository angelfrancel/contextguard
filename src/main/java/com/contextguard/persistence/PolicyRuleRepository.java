package com.contextguard.persistence;

import java.util.List;
import org.springframework.data.repository.Repository;

public interface PolicyRuleRepository
        extends Repository<PolicyRuleEntity, Long> {

    List<PolicyRuleEntity> findAll();
}