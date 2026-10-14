package com.contextguard.demoupstream.tool;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class EchoTools {

    @McpTool(
            name = "echo",
            description = "Returns the provided text unchanged",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    openWorldHint = false
            )
    )
    public String echo(
            @McpToolParam(
                    description = "Text to return",
                    required = true
            ) String text
    ) {
        return text;
    }

}
