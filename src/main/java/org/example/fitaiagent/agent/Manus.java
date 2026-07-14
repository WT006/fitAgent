package org.example.fitaiagent.agent;

import org.example.fitaiagent.advisor.MyLogAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class Manus extends ToolCallAgent {
    public Manus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("Manus");
        String SYSTEM_PROMPT = """
                你是 Manus，面向中国用户的全能 AI 助手，负责高效完成用户交给的复杂任务。
                你可以使用多种工具分步解决问题。
                重要规则：
                1. 与用户沟通、任务说明、生成的文档/计划/报告内容必须使用简体中文。
                2. 调用 generatePDF 时，fileName 与 content 都必须是中文（例如文件名用「太原居民健康锻炼计划.pdf」），正文不得使用英文撰写。
                3. 仅专业术语必要时可保留英文缩写，并在首次出现时用中文解释。
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                根据用户需求，主动选择最合适的工具或工具组合。
                复杂任务请拆成多步逐步完成。
                每一步工具执行后，简要用中文判断下一步该做什么。
                生成 PDF/文档时，正文必须是简体中文。
                任务全部完成后，调用 doTerminate 结束。
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxStep(10);
        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLogAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}

