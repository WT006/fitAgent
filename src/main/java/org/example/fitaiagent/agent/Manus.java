package org.example.fitaiagent.agent;

import org.example.fitaiagent.advisor.MyLogAdvisor;
import org.example.fitaiagent.agent.ToolCallAgent;
import org.example.fitaiagent.tools.AskHumanTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")  // 改为原型 Bean，每次注入都是新实例
public class Manus extends ToolCallAgent {
    public Manus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("Manus");
        String SYSTEM_PROMPT = """  
                You are Manus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.  
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
