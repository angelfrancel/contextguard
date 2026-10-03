# ADR-0002: Controlled MCP Tool Forwarding

- **Status:** Accepted
- **Date:** 2026-10-03
- **Decision owners:** ContextGuard project
- **Related:** ADR-0001 Technology Baseline

## Context

ContextGuard is an MCP gateway. It acts as an MCP server toward callers and as an MCP client toward one configured upstream MCP server.

Spring AI can automatically expose tools discovered through an MCP client by setting:

```properties
spring.ai.mcp.server.expose-mcp-client-tools=true
```

That shortcut would make the upstream tools visible through ContextGuard with little application code. However, ContextGuard must inspect tool arguments, evaluate privacy policy, produce an `ALLOW`, `REDACT`, or `BLOCK` decision, audit safely, and forward only permitted arguments. Automatic re-exposure does not provide the explicit application-controlled boundary required for that pipeline.

The MVP also supports exactly one upstream MCP server. Silently selecting one client from multiple configured connections could route sensitive data to the wrong destination.

## Decision

ContextGuard will keep automatic MCP client-tool exposure disabled.

Incoming MCP operations will pass through explicit ContextGuard-owned orchestration before any upstream call. The gateway will use Spring AI's synchronous MCP client API through a small upstream adapter. That adapter will enforce the MVP invariant that exactly one upstream client is available.

The initial vertical slice uses an explicit `echo` gateway tool only to prove the complete transport path:

```text
MCP caller
  → ContextGuard MCP server
  → ContextGuard-controlled gateway handler
  → upstream adapter
  → demo upstream MCP server
  → result returned through ContextGuard
```

The static `echo` handler is experimental scaffolding. The completed gateway will implement the supported MCP operations and insert the privacy-policy pipeline before forwarding `tools/call` arguments. It will not require one hard-coded Java method for every future upstream tool.

## Alternatives considered

### Enable automatic client-tool exposure

**Advantages**

- Minimal code.
- Upstream tools become visible automatically.
- Useful for applications that only aggregate MCP tools.

**Disadvantages**

- Does not establish an explicit ContextGuard-controlled policy boundary.
- Risks forwarding calls without the required detection, decision, redaction, and audit sequence.
- Makes it harder to demonstrate and test ContextGuard's central responsibility.

**Decision:** Rejected for ContextGuard.

### Implement a transparent HTTP proxy

**Advantages**

- Direct control over raw HTTP traffic.
- Potentially small transport layer for simple requests.

**Disadvantages**

- ContextGuard would need to manage MCP session semantics, JSON-RPC behavior, protocol negotiation, and Streamable HTTP details directly.
- Higher risk of producing a generic sanitization proxy rather than a genuine MCP gateway.

**Decision:** Rejected for the MVP.

### Use the MCP Java SDK without Spring AI auto-configuration

**Advantages**

- Maximum lifecycle and transport control.
- Fewer framework abstractions.

**Disadvantages**

- More manual client/server lifecycle code.
- More configuration and protocol plumbing to test and maintain.
- Does not currently solve a demonstrated MVP requirement better than the selected Spring AI integration.

**Decision:** Rejected for the MVP; reconsider only if Spring AI prevents required protocol interception.

## Consequences

### Positive

- Every forwarded tool call has an explicit place for policy enforcement.
- The gateway's core responsibility remains visible and testable.
- A single adapter isolates most direct MCP SDK usage.
- Multiple-upstream misconfiguration fails explicitly instead of silently choosing a destination.
- The architecture can be explained clearly during review and interviews.

### Negative

- More application code than automatic tool re-exposure.
- ContextGuard must deliberately map upstream tool discovery and calls to its server-facing behavior.
- Error translation, timeouts, and lifecycle behavior require explicit tests.

## Validation evidence

The decision was validated with Java 21, Spring Boot 4.1.1, Spring AI 2.0.1, and Streamable HTTP:

- The demo upstream started independently on `127.0.0.1:8081`.
- MCP `initialize`, `tools/list`, and `tools/call` succeeded against the demo upstream.
- ContextGuard initialized an MCP client connection to that upstream.
- A caller initialized against ContextGuard on `127.0.0.1:8080`.
- Calling `echo` through ContextGuard returned `hello through contextguard` with `isError=false`.
- The basic ContextGuard application-context test passed with the external MCP client disabled and the upstream stopped.

## Follow-up work

- Replace the static vertical-slice forwarding path with the supported generic MCP gateway operations.
- Insert the plain-Java privacy-policy engine before upstream `tools/call` execution.
- Define controlled MCP error mapping for unavailable, timed-out, and invalid upstream responses.
- Add integration tests that start a real demo upstream rather than relying on a manually running process.