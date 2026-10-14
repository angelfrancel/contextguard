package com.contextguard.gateway;

import com.contextguard.application.GuardedToolCallService;
import com.contextguard.policy.ToolNamePolicy;
import com.contextguard.upstream.UpstreamMcpClient;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Configuration(proxyBeanMethods = false)
public class GuardedUpstreamTools {

    @Bean
    @ConditionalOnProperty(name = "spring.ai.mcp.client.enabled", havingValue = "true", matchIfMissing = true)
    public List<McpServerFeatures.SyncToolSpecification> guardedToolSpecifications(
            UpstreamMcpClient upstreamClient,
            GuardedToolCallService guardedToolCallService
    ) {
        Objects.requireNonNull(upstreamClient);
        Objects.requireNonNull(guardedToolCallService);

        List<McpServerFeatures.SyncToolSpecification> specifications = new ArrayList<>();
        Set<String> names = new HashSet<>();

        for (Tool tool : upstreamClient.listAllTools()) {
            String name = ToolNamePolicy.requireValid(tool.name());
            if (!names.add(name)) {
                throw new IllegalStateException("duplicate upstream tool name: " + name);
            }

            // Reuse the upstream schema, but never reuse an upstream call handler.
            specifications.add(McpServerFeatures.SyncToolSpecification.builder()
                    .tool(tool)
                    .callHandler((exchange, request) -> {
                        if (!name.equals(request.name())) {
                            throw new IllegalArgumentException("tool name does not match registered tool");
                        }
                        return guardedToolCallService.callTool(
                                name,
                                request.arguments() == null ? java.util.Map.of() : request.arguments()
                        );
                    })
                    .build());
        }

        return List.copyOf(specifications);
    }
}
