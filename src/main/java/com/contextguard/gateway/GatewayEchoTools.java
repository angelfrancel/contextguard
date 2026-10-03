package com.contextguard.gateway;

import com.contextguard.upstream.UpstreamMcpClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GatewayEchoTools {

    private final UpstreamMcpClient upstreamClient;

    public GatewayEchoTools(UpstreamMcpClient upstreamClient) {
        this.upstreamClient = upstreamClient;
    }

    @McpTool(
            name = "echo",
            description = "Forwards text through ContextGuard to the configured upstream",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    openWorldHint = false
            )
    )
    public CallToolResult echo(
            @McpToolParam(
                    description = "Text to send to the upstream echo tool",
                    required = true
            ) String text
    ) {
        return upstreamClient.callTool(
                "echo",
                Map.of("text", text)
        );
    }
}