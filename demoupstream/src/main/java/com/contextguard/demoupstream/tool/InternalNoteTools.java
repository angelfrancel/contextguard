package com.contextguard.demoupstream.tool;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class InternalNoteTools {

    @McpTool(
            name = "internal_note",
            description = "Simulates an approved internal note tool by returning the supplied text",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    openWorldHint = false
            )
    )
    public String internalNote(
            @McpToolParam(description = "Text for the simulated note", required = true) String text
    ) {
        return text;
    }
}
