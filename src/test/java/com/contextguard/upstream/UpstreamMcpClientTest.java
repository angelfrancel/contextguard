package com.contextguard.upstream;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpstreamMcpClientTest {

    @Test
    void discoversToolsAcrossAllPages() {
        McpSyncClient client = mock(McpSyncClient.class);
        Tool first = tool("echo");
        Tool second = tool("uppercase");
        when(client.listTools()).thenReturn(new ListToolsResult(List.of(first), "next"));
        when(client.listTools("next")).thenReturn(new ListToolsResult(List.of(second), null));

        assertEquals(List.of(first, second), new UpstreamMcpClient(List.of(client)).listAllTools());
    }

    @Test
    void rejectsMoreThanOneConfiguredUpstream() {
        UpstreamMcpClient client = new UpstreamMcpClient(
                List.of(mock(McpSyncClient.class), mock(McpSyncClient.class))
        );

        assertThrows(IllegalStateException.class, client::listAllTools);
    }

    @Test
    void rejectsPaginationLoops() {
        McpSyncClient client = mock(McpSyncClient.class);
        when(client.listTools()).thenReturn(new ListToolsResult(List.of(), "next"));
        when(client.listTools("next")).thenReturn(new ListToolsResult(List.of(), "next"));

        assertThrows(IllegalStateException.class,
                () -> new UpstreamMcpClient(List.of(client)).listAllTools());
    }

    private Tool tool(String name) {
        return Tool.builder(name).inputSchema(Map.of("type", "object")).build();
    }
}
