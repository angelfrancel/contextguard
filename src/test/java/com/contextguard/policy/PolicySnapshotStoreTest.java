package com.contextguard.policy;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicySnapshotStoreTest {

    @Test
    void returnsInitialSnapshot() {
        PolicySnapshot initialSnapshot = snapshot(1L);
        PolicySnapshotStore store =
                new PolicySnapshotStore(initialSnapshot);

        PolicySnapshot result = store.current();

        assertSame(initialSnapshot, result);
    }

    @Test
    void replacesCurrentSnapshot() {
        PolicySnapshot initialSnapshot = snapshot(1L);
        PolicySnapshot replacementSnapshot = snapshot(2L);

        PolicySnapshotStore store =
                new PolicySnapshotStore(initialSnapshot);

        store.replace(replacementSnapshot);

        assertSame(replacementSnapshot, store.current());
        assertEquals(2L, store.current().version());
    }

    @Test
    void rejectsNullInitialSnapshot() {
        assertThrows(
                NullPointerException.class,
                () -> new PolicySnapshotStore(null)
        );
    }

    @Test
    void rejectsNullReplacementSnapshot() {
        PolicySnapshotStore store =
                new PolicySnapshotStore(snapshot(1L));

        assertThrows(
                NullPointerException.class,
                () -> store.replace(null)
        );
    }

    private PolicySnapshot snapshot(long version) {
        return new PolicySnapshot(
                version,
                Map.of(
                        DetectionType.EMAIL,
                        PolicyOutcome.REDACT
                )
        );
    }
}