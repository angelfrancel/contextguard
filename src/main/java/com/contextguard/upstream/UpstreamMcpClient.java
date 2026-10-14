package com.contextguard.upstream;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UpstreamMcpClient {

    private final List<McpSyncClient> clients;

    public UpstreamMcpClient(List<McpSyncClient> clients) {
        this.clients = List.copyOf(clients);
    }

    public ListToolsResult listTools() {
        return singleClient().listTools();
    }

    public List<Tool> listAllTools() {
        McpSyncClient client = singleClient();
        List<Tool> tools = new ArrayList<>();
        Set<String> seenCursors = new HashSet<>();
        String cursor = null;

        do {
            ListToolsResult page = cursor == null ? client.listTools() : client.listTools(cursor);
            tools.addAll(page.tools());
            cursor = page.nextCursor();
            if (cursor != null && !cursor.isBlank() && !seenCursors.add(cursor)) {
                throw new IllegalStateException("upstream tool pagination repeated a cursor");
            }
        } while (cursor != null && !cursor.isBlank());

        return List.copyOf(tools);
    }

    public CallToolResult callTool(
            String toolName,
            Map<String, Object> arguments
    ) {
        CallToolRequest request = CallToolRequest.builder(toolName)
                .arguments(arguments)
                .build();

        return singleClient().callTool(request);
    }

    private McpSyncClient singleClient() {
        if (clients.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one upstream MCP client but found "
                            + clients.size()
            );
        }

        return clients.getFirst();
    }
}
