# ADR 0001 Technology Baseline

## Status

Proposed

## Context

The original ContextGuard specification selected Java 17, Spring Boot 3, and a compatible Spring AI release as the implementation baseline. Before development began, the baseline was reconsidered because the project had no existing production code or migration constraints, and using a current stable technology generation would improve its future relevance.

ContextGuard must operate as both an MCP server toward an external MCP client and an MCP client toward one configured upstream MCP server. The selected technology combination must therefore support both MCP roles, Streamable HTTP, Java-based development, Maven dependency management, and a synchronous implementation style suitable for Spring Data JPA and PostgreSQL.

The dependency combination must be verified as a complete set. Individual versions must not be selected solely because they are the newest available. The project requires a stable, documented, and reproducible combination that can be built with the Maven Wrapper and used consistently in IntelliJ IDEA, automated tests, and Docker.

## Considered Options

### Option 1 Java 17 Spring Boot 3.5 and Spring AI 1.1

This option retains the original specification's Java and Spring Boot generations while using the stable Spring AI line intended for Spring Boot 3.5.

Advantages:

- Preserves the original Java 17 and Spring Boot 3 requirements.
- Uses mature platform versions with broad ecosystem support.
- Reduces the risk associated with adopting a newer major Spring generation.
- Supports MCP client and server functionality, including Streamable HTTP.

Disadvantages:

- Starts a new project on an older Java and Spring generation even though no migration constraint exists.
- Requires careful use of Spring AI 1.1 documentation because current examples may target Spring AI 2.
- Provides a shorter forward-looking baseline for a portfolio project that will continue after the MVP.

### Option 2 Java 21 Spring Boot 4.1 and Spring AI 2.0

This option adopts Java 21 LTS, Spring Boot 4.1.1, and Spring AI 2.0.1 as a current stable technology generation.

Advantages:

- Uses a modern Java LTS release with broad tooling and ecosystem support.
- Uses the current Spring AI major generation and its MCP integration.
- Avoids a later major-version migration because the project has not yet accumulated implementation code.
- Provides a more current foundation for continued development after the 12-day MVP.

Disadvantages:

- Uses a newer Spring Boot and Spring AI generation with less project-specific evidence at the time of this decision.
- Requires verification that MCP server and client auto-configuration can coexist in one application.
- May require lower-level MCP SDK configuration if Spring AI starters do not provide the policy interception point required by ContextGuard.
- Deliberately changes the baseline recorded in the original project specification.

## Decision

ContextGuard will use the following candidate technology baseline:

- Java 21 LTS
- Spring Boot 4.1.1
- Spring AI 2.0.1
- Maven with the Maven Wrapper
- Spring AI Bill of Materials for compatible Spring AI and MCP module versions
- Spring WebMVC
- Synchronous MCP server and client APIs
- MCP Streamable HTTP transport
- PostgreSQL for persistent rule configuration and audit metadata
- Spring Data JPA for persistence access
- Flyway as the only schema migration mechanism

Spring AI Boot starters will be used initially for MCP server and client infrastructure. ContextGuard will use lower-level MCP Java SDK APIs only where the starters cannot provide the explicit control required for tool catalog representation, policy interception, forwarding, or lifecycle management.

WebMVC and synchronous APIs were selected because the MVP uses blocking PostgreSQL access through Spring Data JPA. A reactive WebFlux design would add complexity without providing its full non-blocking benefit while the persistence path remains blocking.

The Spring Boot parent will manage the main Spring ecosystem and Maven plugin defaults. The Spring AI BOM will manage Spring AI and related MCP module versions. Individually managed versions will not override the BOM without a documented compatibility reason.

## Consequences

Positive:

- The project begins on a modern Java LTS and current Spring generation.
- MCP dependencies can be managed as a compatible set rather than as unrelated individual versions.
- WebMVC provides a straightforward request-response and debugging model for the 12-day MVP.
- The application can use Spring Boot lifecycle management and auto-configuration rather than recreating MCP infrastructure.
- The baseline remains suitable for continued development after the initial resume-ready release.

Negative:

- The original Java 17 and Spring Boot 3 baseline is superseded and must not be used as implementation guidance after this ADR is accepted.
- Current documentation and examples must be checked against the exact pinned versions before dependencies, imports, or properties are copied.
- Auto-configuration may not expose every control required by a policy-enforcement gateway.
- If the starter-based experiment fails, part of the MCP integration may need to use programmatic SDK configuration.
- Synchronous upstream and database operations occupy request threads while they wait, so concurrency and latency must be measured before making performance claims.

## Validation

This ADR will change from Proposed to Accepted only after all of the following evidence exists:

1. Maven resolves the Spring Boot 4.1.1 and Spring AI 2.0.1 dependency combination through the Maven Wrapper.
2. The project compiles and its automated tests pass using Java 21.
3. The Spring application context starts successfully with the MCP server and client dependencies present.
4. A harmless demo upstream MCP server starts over Streamable HTTP.
5. A compatible MCP client initializes with the demo upstream and successfully lists and calls a harmless tool directly.
6. ContextGuard starts with both MCP server-side and client-side functionality.
7. A compatible MCP client completes a harmless tool call through ContextGuard to the upstream server and receives the result.
8. The exact dependency artifacts, configuration properties, MCP endpoint, and supported protocol behavior are documented.

If the experiment fails, this ADR will remain Proposed while the failure is investigated. Any replacement version combination or implementation approach must be recorded as an amendment or superseding ADR rather than changed silently.

## References

- Spring AI version compatibility: https://github.com/spring-projects/spring-ai
- Spring AI MCP documentation: https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html
- Spring AI MCP getting started guide: https://docs.spring.io/spring-ai/reference/guides/getting-started-mcp.html
- Spring Boot documentation: https://docs.spring.io/spring-boot/
- Model Context Protocol specification: https://modelcontextprotocol.io/specification/
