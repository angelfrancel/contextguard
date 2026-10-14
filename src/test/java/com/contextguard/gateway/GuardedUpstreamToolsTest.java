package com.contextguard.gateway;

import com.contextguard.application.GuardedToolCallService;
import com.contextguard.upstream.UpstreamMcpClient;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GuardedUpstreamToolsTest {

    private final UpstreamMcpClient upstream = mock(UpstreamMcpClient.class);
    private final GuardedToolCallService guard = mock(GuardedToolCallService.class);
    private final GuardedUpstreamTools configuration = new GuardedUpstreamTools();

    @Test
    void exposesTwoUpstreamSchemasButRoutesBothCallsThroughGuard() {
        Tool echo = tool("echo");
        Tool uppercase = tool("uppercase");
        when(upstream.listAllTools()).thenReturn(List.of(echo, uppercase));
        CallToolResult response = CallToolResult.builder().addTextContent("safe").build();
        when(guard.callTool("echo", Map.of("text", "hello"))).thenReturn(response);
        when(guard.callTool("uppercase", Map.of("text", "hello"))).thenReturn(response);

        List<SyncToolSpecification> specifications =
                configuration.guardedToolSpecifications(upstream, guard);

        assertEquals(2, specifications.size());
        assertSame(echo, specifications.get(0).tool());
        assertSame(uppercase, specifications.get(1).tool());
        assertSame(response, specifications.get(0).callHandler().apply(
                null, CallToolRequest.builder("echo")
                        .arguments(Map.of("text", "hello")).build()));
        assertSame(response, specifications.get(1).callHandler().apply(
                null, CallToolRequest.builder("uppercase")
                        .arguments(Map.of("text", "hello")).build()));
        verify(guard).callTool("echo", Map.of("text", "hello"));
        verify(guard).callTool("uppercase", Map.of("text", "hello"));
    }

    @Test
    void rejectsDuplicateNamesInsteadOfExposingOnlyOne() {
        when(upstream.listAllTools()).thenReturn(List.of(tool("echo"), tool("echo")));

        assertThrows(IllegalStateException.class,
                () -> configuration.guardedToolSpecifications(upstream, guard));
        verifyNoInteractions(guard);
    }

    @Test
    void rejectsUnsupportedToolNamesBeforeRegistration() {
        when(upstream.listAllTools()).thenReturn(List.of(tool("invalid name")));

        assertThrows(IllegalArgumentException.class,
                () -> configuration.guardedToolSpecifications(upstream, guard));
        verifyNoInteractions(guard);
    }

    private Tool tool(String name) {
        return Tool.builder(name)
                .description("Example tool")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of("text", Map.of("type", "string")),
                        "required", List.of("text")
                ))
                .build();
    }
}
