ALTER TABLE policy_rules
    ADD COLUMN tool_name VARCHAR(128),
    ADD CONSTRAINT chk_policy_rules_tool_name
        CHECK (tool_name IS NULL OR tool_name ~ '^[A-Za-z0-9._-]{1,128}$');

ALTER TABLE policy_rules DROP CONSTRAINT policy_rules_detection_type_key;

CREATE UNIQUE INDEX uq_policy_rules_default_type
    ON policy_rules (detection_type)
    WHERE tool_name IS NULL;

CREATE UNIQUE INDEX uq_policy_rules_tool_and_type
    ON policy_rules (tool_name, detection_type)
    WHERE tool_name IS NOT NULL;

-- Simulated approved internal tool: allow email while other tools retain the default REDACT rule.
INSERT INTO policy_rules (tool_name, detection_type, outcome, enabled)
VALUES ('internal_note', 'EMAIL', 'ALLOW', TRUE);
