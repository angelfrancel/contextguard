package com.contextguard.policy;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class PolicySnapshotStore {

    private final AtomicReference<PolicySnapshot> currentSnapshot;

    public PolicySnapshotStore(PolicySnapshot initialSnapshot) {
        Objects.requireNonNull(initialSnapshot, "initialSnapshot must not be null");

        this.currentSnapshot = new AtomicReference<>(initialSnapshot);
    }

    public PolicySnapshot current() {
        return currentSnapshot.get();
    }

    public void replace(PolicySnapshot newSnapshot) {
        Objects.requireNonNull(newSnapshot, "newSnapshot must not be null");
        currentSnapshot.set(newSnapshot);
    }
}