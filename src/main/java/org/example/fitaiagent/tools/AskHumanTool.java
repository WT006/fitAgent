package org.example.fitaiagent.tools;

import org.example.fitaiagent.agent.BaseAgent;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class AskHumanTool {

    // 使用 ThreadLocal 存储当前执行的 Agent
    private static final ThreadLocal<BaseAgent> currentAgentHolder = new ThreadLocal<>();

    // 设置当前 Agent（由 Agent 在执行时调用）
    public static void setCurrentAgent(BaseAgent agent) {
        currentAgentHolder.set(agent);
    }

    // 清除当前 Agent（执行结束后调用）
    public static void clearCurrentAgent() {
        currentAgentHolder.remove();
    }

    @Tool(description = "Use this tool to ask human for help. The agent will pause and wait for human response.")
    public String askHuman(@ToolParam(description = "The question you want to ask human.") String inquire) {
        BaseAgent agent = currentAgentHolder.get();
        if (agent == null) {
            return "[ERROR] 无法获取当前 Agent 上下文";
        }

        // 触发 Agent 暂停
        agent.pauseForHumanInput(inquire);

        return "[PAUSED_FOR_HUMAN_INPUT] 已暂停，等待人类回复";
    }
}
