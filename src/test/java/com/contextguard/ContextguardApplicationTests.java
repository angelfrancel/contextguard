package com.contextguard;

import com.contextguard.policy.DetectionType;
import com.contextguard.policy.PolicyOutcome;
import com.contextguard.policy.PolicySnapshotStore;
import com.contextguard.persistence.PolicyAuditWriter;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.ai.mcp.client.enabled=false")
class ContextguardApplicationTests {

	@Autowired
	private PolicySnapshotStore snapshotStore;

	@Autowired
	private PolicyAuditWriter auditWriter;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
		assertEquals(
				Map.of(
						DetectionType.EMAIL, PolicyOutcome.REDACT,
						DetectionType.PHILIPPINE_MOBILE_NUMBER, PolicyOutcome.REDACT,
						DetectionType.PAYMENT_CARD, PolicyOutcome.REDACT,
						DetectionType.AUTHENTICATION_TOKEN, PolicyOutcome.BLOCK,
						DetectionType.SENSITIVE_JSON_FIELD, PolicyOutcome.BLOCK
				),
				snapshotStore.current().outcomesByType()
		);
	}

	@Test
	@Transactional
	void auditWriterPersistsOnlyDecisionMetadata() {
		assertTrue(AopUtils.isAopProxy(auditWriter));
		String toolName = "test.audit_" + System.nanoTime();
		auditWriter.record(toolName, PolicyOutcome.REDACT, 1, snapshotStore.current().version());

		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM policy_audit_events WHERE tool_name = ? "
						+ "AND outcome = 'REDACT' AND detection_count = 1 AND snapshot_version = ?",
				Integer.class,
				toolName,
				snapshotStore.current().version()
		);
		assertEquals(1, count);
	}

}
