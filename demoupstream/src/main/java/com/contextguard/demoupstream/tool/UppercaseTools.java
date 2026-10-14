package com.contextguard.demoupstream.tool;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UppercaseTools {

    @McpTool(
            name = "uppercase",
            description = "Returns the provided text in uppercase",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    openWorldHint = false
            )
    )
    public String uppercase(
            @McpToolParam(description = "Text to convert", required = true) String text
    ) {
        return text.toUpperCase(Locale.ROOT);
    }
}
