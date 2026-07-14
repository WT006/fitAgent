package org.example.fitaiagent.agent;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.agent.model.AgentState;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    /** 写入下一轮 LLM 上下文的单条工具结果上限，过大易导致后续 function.arguments 非法 */
    private static final int MAX_TOOL_RESULT_CHARS = 2500;

    private final ToolCallback[] availableTools;

    private ChatResponse toolCallChatResponse;

    private final ToolCallingManager toolCallingManager;

    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    @Override
    public boolean think() {
        appendNextStepPromptOnce();
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s",
                            toolCall.name(),
                            toolCall.arguments())
                    )
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);

            // 过滤非法 arguments，避免下一轮请求被 DashScope 400
            List<AssistantMessage.ToolCall> validCalls = toolCallList.stream()
                    .filter(this::hasValidJsonArguments)
                    .toList();
            if (!validCalls.isEmpty() && validCalls.size() != toolCallList.size()) {
                log.warn("{} 丢弃了 {} 个非法 arguments 的工具调用",
                        getName(), toolCallList.size() - validCalls.size());
            }

            if (toolCallList.isEmpty()) {
                getMessageList().add(assistantMessage);
                return false;
            }
            if (validCalls.isEmpty()) {
                getMessageList().add(new AssistantMessage(
                        "工具参数格式无效，已跳过本轮工具调用。请用 JSON 对象形式传参后重试。"));
                return false;
            }
            // 若存在非法调用，重建仅含合法 toolCalls 的响应较复杂；此处仅在全部合法时继续 act
            if (validCalls.size() != toolCallList.size()) {
                getMessageList().add(new AssistantMessage(
                        "部分工具参数不是合法 JSON，已中止本轮工具执行。请继续并以 JSON 传参。"));
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessageList().add(
                    new AssistantMessage("处理时遇到错误: " + summarizeError(e.getMessage())));
            return false;
        }
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        List<Message> history = toolExecutionResult.conversationHistory();
        setMessageList(compactConversationHistory(history));

        ToolResponseMessage toolResponseMessage = findLastToolResponse(getMessageList());
        if (toolResponseMessage == null) {
            toolResponseMessage = findLastToolResponse(history);
        }
        if (toolResponseMessage == null) {
            return "工具执行完成";
        }

        String resultsForLog = toolResponseMessage.getResponses().stream()
                .map(response -> {
                    String data = StrUtil.blankToDefault(response.responseData(), "");
                    String preview = data.length() > 300 ? data.substring(0, 300) + "…(日志已截断)" : data;
                    return "工具 " + response.name() + " 完成了它的任务！结果: " + preview;
                })
                .collect(Collectors.joining("\n"));
        // 前端只展示简短状态；PDF 成功时保留 downloadKey 供下载按钮解析
        String resultsForClient = toolResponseMessage.getResponses().stream()
                .map(this::formatToolResultForClient)
                .collect(Collectors.joining("\n"));

        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(resultsForLog);
        return resultsForClient;
    }

    private String formatToolResultForClient(ToolResponseMessage.ToolResponse response) {
        String data = StrUtil.blankToDefault(response.responseData(), "");
        if (data.contains("downloadKey=")) {
            return data;
        }
        if (StrUtil.startWithIgnoreCase(data, "Error") || data.contains("Error generating")
                || data.contains("执行失败") || data.contains("not recognized")) {
            return "tool=" + response.name() + " status=failed";
        }
        return "tool=" + response.name() + " status=done";
    }

    private ToolResponseMessage findLastToolResponse(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message instanceof ToolResponseMessage toolResponseMessage) {
                return toolResponseMessage;
            }
        }
        return null;
    }

    /**
     * 每轮只保留一条 nextStepPrompt，避免用户消息重复堆叠撑爆上下文
     */
    private void appendNextStepPromptOnce() {
        String next = getNextStepPrompt();
        if (StrUtil.isBlank(next)) {
            return;
        }
        getMessageList().removeIf(m -> m instanceof UserMessage um && next.equals(um.getText()));
        getMessageList().add(new UserMessage(next));
    }

    private boolean hasValidJsonArguments(AssistantMessage.ToolCall toolCall) {
        String args = toolCall.arguments();
        if (StrUtil.isBlank(args)) {
            // 无参工具偶发返回空串；视为 "{}"
            return true;
        }
        String trimmed = args.trim();
        if ("{}".equals(trimmed) || "[]".equals(trimmed) || "null".equalsIgnoreCase(trimmed)) {
            return true;
        }
        return JSONUtil.isTypeJSONObject(trimmed) || JSONUtil.isTypeJSONArray(trimmed);
    }

    private List<Message> compactConversationHistory(List<Message> history) {
        List<Message> compacted = new ArrayList<>(history.size());
        for (Message message : history) {
            if (message instanceof ToolResponseMessage toolResponseMessage) {
                List<ToolResponseMessage.ToolResponse> responses = toolResponseMessage.getResponses().stream()
                        .map(this::compactToolResponse)
                        .toList();
                compacted.add(ToolResponseMessage.builder().responses(responses).build());
            } else {
                compacted.add(message);
            }
        }
        return compacted;
    }

    private ToolResponseMessage.ToolResponse compactToolResponse(ToolResponseMessage.ToolResponse response) {
        String data = response.responseData();
        if (data == null || data.length() <= MAX_TOOL_RESULT_CHARS || data.contains("downloadKey=")) {
            return response;
        }
        return new ToolResponseMessage.ToolResponse(
                response.id(),
                response.name(),
                data.substring(0, MAX_TOOL_RESULT_CHARS) + "…(已截断，完整细节可按需再次查询)"
        );
    }

    private String summarizeError(String message) {
        if (StrUtil.isBlank(message)) {
            return "未知错误";
        }
        if (message.contains("function.arguments")) {
            return "模型返回的工具参数不是合法 JSON（DashScope InvalidParameter）。请换种说法重试，或简化任务步骤。";
        }
        return message.length() > 200 ? message.substring(0, 200) + "…" : message;
    }
}
