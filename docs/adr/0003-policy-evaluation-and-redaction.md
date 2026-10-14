# ADR-0003: Policy Evaluation and Safe Redaction

- **Status:** Accepted
- **Date:** 2026-10-12
- **Decision owners:** ContextGuard project
- **Related:** ADR-0002 Controlled MCP Tool Forwarding

## Context

MCP tool arguments can contain sensitive values in strings, nested objects, and lists. ContextGuard needs a deterministic decision for each call and must know which detections require redaction. The policy engine must be testable without a database or MCP connection.

## Decision

Enabled rules are compiled into an immutable `PolicySnapshot` keyed by detection type. Duplicate enabled rules for one type are rejected. A `PolicySnapshotStore` replaces the current snapshot atomically. An evaluation uses a supplied snapshot, so a call can use one consistent policy version.

The evaluator resolves `BLOCK` before `REDACT` before `ALLOW`. A detection without a configured rule fails closed as `BLOCK`. Its `PolicyDecision` contains the overall outcome and only the detections whose rules require redaction. A `BLOCK` decision carries no redaction detections.

For a `REDACT` decision, whole-field detections replace the entire field value with `[REDACTED]`. Text detections replace only their ranges. Overlapping ranges are merged before replacement, and replacements run from right to left so earlier indexes remain valid. `ArgumentRedactor` recursively creates new maps and lists, leaving the original arguments unchanged. A detection path that cannot be applied is rejected rather than silently forwarded.

The gateway checks for `BLOCK` before any upstream `tools/call`. For `ALLOW`, it forwards a validated copy of the arguments. For `REDACT`, it forwards the sanitized copy. Every tool discovered from the configured upstream uses this guarded path. It writes an audit record containing only the tool name, outcome, detection count, snapshot version, and timestamp before forwarding. If the audit write fails, the call does not reach the upstream server.

## Alternatives considered

### Query rules for every tool call

This would make each call depend on database availability and add database latency to the forwarding path. The in-memory snapshot keeps evaluation independent of a per-call query while allowing an explicit refresh.

### Modify the caller's argument map in place

This would risk changing data still held by the caller and sharing partially redacted nested objects. Recursive copying makes the forwarded value separate from the original.

### Replace detected strings from left to right

Changing a string's length would invalidate later offsets. Merging overlaps and replacing from right to left preserves the original ranges.

## Consequences

- Policy evaluation and redaction can be unit tested without Spring or an upstream server.
- Missing rules and invalid redaction paths stop processing instead of silently allowing sensitive data through.
- Snapshot refresh must compile a complete valid replacement before publishing it.
- Every exposed gateway tool must use the guarded call service; future tools must not call the upstream adapter directly.

## Validation evidence

On 2026-10-12, the full Maven test suite passed: 91 tests, 0 failures, 0 errors, and 0 skipped. Tests cover outcome precedence, selected redaction detections, overlapping and separate text ranges, nested field and list redaction, preservation of original arguments, and rejection of an unknown detection path.
