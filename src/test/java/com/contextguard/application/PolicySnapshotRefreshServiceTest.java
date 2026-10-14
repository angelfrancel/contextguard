package com.contextguard.application;

import com.contextguard.persistence.PolicyRuleLoader;
import com.contextguard.policy.DetectionType;
import com.contextguard.policy.PolicyOutcome;
import com.contextguard.policy.PolicyRule;
import com.contextguard.policy.PolicySnapshot;
import com.contextguard.policy.PolicySnapshotCompiler;
import com.contextguard.policy.PolicySnapshotStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicySnapshotRefreshServiceTest {

    @Test
    void replacesSnapshotOnlyAfterLoadingAndCompilingRules() {
        PolicySnapshot initial = initialSnapshot();
        PolicySnapshotStore store = new PolicySnapshotStore(initial);
        PolicyRuleLoader loader = mock(PolicyRuleLoader.class);
        when(loader.loadAll()).thenReturn(List.of(
                new PolicyRule(1L, DetectionType.EMAIL, PolicyOutcome.BLOCK, true)
        ));
        PolicySnapshotRefreshService service = new PolicySnapshotRefreshService(
                loader, new PolicySnapshotCompiler(), store
        );

        PolicySnapshot refreshed = service.refresh();

        assertSame(refreshed, store.current());
        assertEquals(2L, refreshed.version());
        assertEquals(Optional.of(PolicyOutcome.BLOCK), refreshed.outcomeFor(DetectionType.EMAIL));
        assertEquals(Optional.of(PolicyOutcome.REDACT), initial.outcomeFor(DetectionType.EMAIL));
    }

    @Test
    void keepsPreviousSnapshotWhenLoadingFails() {
        PolicySnapshot initial = initialSnapshot();
        PolicySnapshotStore store = new PolicySnapshotStore(initial);
        PolicyRuleLoader loader = mock(PolicyRuleLoader.class);
        when(loader.loadAll()).thenThrow(new IllegalStateException("database unavailable"));
        PolicySnapshotRefreshService service = new PolicySnapshotRefreshService(
                loader, new PolicySnapshotCompiler(), store
        );

        assertThrows(IllegalStateException.class, service::refresh);

        assertSame(initial, store.current());
        assertEquals(1L, store.current().version());
    }

    @Test
    void keepsPreviousSnapshotWhenCompilationFails() {
        PolicySnapshot initial = initialSnapshot();
        PolicySnapshotStore store = new PolicySnapshotStore(initial);
        PolicyRuleLoader loader = mock(PolicyRuleLoader.class);
        when(loader.loadAll()).thenReturn(List.of(
                new PolicyRule(1L, DetectionType.EMAIL, PolicyOutcome.REDACT, true),
                new PolicyRule(2L, DetectionType.EMAIL, PolicyOutcome.BLOCK, true)
        ));
        PolicySnapshotRefreshService service = new PolicySnapshotRefreshService(
                loader, new PolicySnapshotCompiler(), store
        );

        assertThrows(IllegalArgumentException.class, service::refresh);

        assertSame(initial, store.current());
        assertEquals(1L, store.current().version());
    }

    private PolicySnapshot initialSnapshot() {
        return new PolicySnapshot(1L, Map.of(DetectionType.EMAIL, PolicyOutcome.REDACT));
    }
}
