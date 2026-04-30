package org.example.fitaiagent.tools;

import org.example.fitaiagent.tools.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Autowired(required = false)
    private ToolCallbackProvider mcpToolCallbackProvider;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        AskHumanTool askHumanTool = new AskHumanTool();

        List<ToolCallback> allToolsList = new ArrayList<>();

        allToolsList.addAll(List.of(ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool,
                askHumanTool
        )));

        if (mcpToolCallbackProvider != null) {
            ToolCallback[] mcpTools = mcpToolCallbackProvider.getToolCallbacks();
            if (mcpTools != null && mcpTools.length > 0) {
                allToolsList.addAll(List.of(mcpTools));
            }
        }

        return allToolsList.toArray(new ToolCallback[0]);
    }
}
