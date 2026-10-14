package com.contextguard.config;

import com.contextguard.persistence.PolicyRuleLoader;
import com.contextguard.policy.PolicySnapshotCompiler;
import com.contextguard.policy.PolicySnapshotStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PolicyConfiguration {

    @Bean
    public PolicySnapshotCompiler policySnapshotCompiler() {
        return new PolicySnapshotCompiler();
    }

    @Bean
    public PolicySnapshotStore policySnapshotStore(
            PolicyRuleLoader loader,
            PolicySnapshotCompiler compiler
    ) {
        return new PolicySnapshotStore(compiler.compile(1L, loader.loadAll()));
    }
}
