package com.contextguard.application;

import com.contextguard.persistence.PolicyRuleLoader;
import com.contextguard.policy.PolicyRule;
import com.contextguard.policy.PolicySnapshot;
import com.contextguard.policy.PolicySnapshotCompiler;
import com.contextguard.policy.PolicySnapshotStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class PolicySnapshotRefreshService {

    private final PolicyRuleLoader loader;
    private final PolicySnapshotCompiler compiler;
    private final PolicySnapshotStore store;

    public PolicySnapshotRefreshService(
            PolicyRuleLoader loader,
            PolicySnapshotCompiler compiler,
            PolicySnapshotStore store
    ) {
        this.loader = Objects.requireNonNull(loader, "loader must not be null");
        this.compiler = Objects.requireNonNull(compiler, "compiler must not be null");
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    public synchronized PolicySnapshot refresh() {
        long nextVersion = Math.incrementExact(store.current().version());
        List<PolicyRule> rules = loader.loadAll();
        PolicySnapshot refreshed = compiler.compile(nextVersion, rules);
        store.replace(refreshed);
        return refreshed;
    }
}
