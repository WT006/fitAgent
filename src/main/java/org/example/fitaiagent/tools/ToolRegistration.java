package org.example.fitaiagent.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    private final PDFGenerationTool pdfGenerationTool;

    @Autowired(required = false)
    private ToolCallbackProvider mcpToolCallbackProvider;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        TerminateTool terminateTool = new TerminateTool();

        List<ToolCallback> allToolsList = new ArrayList<>();

        allToolsList.addAll(List.of(ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool
        )));

        if (mcpToolCallbackProvider != null) {
            log.info("开始注入MCP");
            ToolCallback[] mcpTools = mcpToolCallbackProvider.getToolCallbacks();
            if (mcpTools != null && mcpTools.length > 0) {
                allToolsList.addAll(List.of(mcpTools));
            }
        }

        return allToolsList.toArray(new ToolCallback[0]);
    }
}
