# ADR-0004: Tool-Specific Policy Rules

- **Status:** Accepted
- **Date:** 2026-10-28
- **Related:** ADR-0003 Policy Evaluation and Safe Redaction

## Context

A detection type alone does not determine whether data should be sent to a tool. An approved internal tool may need an email address, while an unrelated tool should receive a redacted value. ContextGuard previously applied the same decision to every tool exposed by its one configured upstream.

## Decision

Policy rules have an optional `tool_name` alongside `detection_type`. A null tool name denotes a default rule for that detection type. A matching rule for the requested tool takes precedence over the default. If neither rule exists for a detection, the decision remains `BLOCK`.

Enabled rules must be unique for each `(tool_name, detection_type)` pair, and default rules remain unique per detection type. The compiled snapshot holds both sets of outcomes. Each tool call reads one snapshot and resolves every detection using the requested tool name before applying the existing `BLOCK` > `REDACT` > `ALLOW` precedence.

The `internal_note` demo tool simulates an approved destination: its `EMAIL` rule is `ALLOW`, while `echo` and `uppercase` continue to use the default `EMAIL` rule of `REDACT`. The default `AUTHENTICATION_TOKEN` rule still blocks all three tools. The demo tool returns its input and does not store notes.

## Consequences

- A tool-specific exception can permit a legitimate workflow without weakening the default for other tools.
- Invalid or duplicate enabled rules prevent a new snapshot from being published.
- Tool names are matched exactly and validated before evaluation.
- This is policy by tool name within one upstream. Distinct upstream destinations, authenticated caller roles, and administrative rule editing remain separate work.
