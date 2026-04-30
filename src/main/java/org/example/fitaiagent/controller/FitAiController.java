package org.example.fitaiagent.controller;

import jakarta.annotation.Resource;
import org.example.fitaiagent.agent.Manus;
import org.example.fitaiagent.app.FitApp;
import org.example.fitaiagent.tools.AskHumanTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/fitai")
public class FitAiController {

    @Resource
    private FitApp fitApp;

    @Resource
    private ToolCallback[] toolcallbacks;

    @Resource
    private ChatModel dashscopeChatModel;

    @Autowired
    private ApplicationContext applicationContext;  // 用于获取原型 Bean

    // 存储活跃的 Agent 实例，key 为 chatId
    private final ConcurrentHashMap<String, Manus> activeAgents = new ConcurrentHashMap<>();

    /*
     * 同步接口
     */
    @GetMapping("/fitapp/chat/sync")
    public String chat(String message, String chatId) {
        return fitApp.doChat(message, chatId);
    }

    /*
     * 流式接口
     */
    @GetMapping(value = "/fitapp/chat/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(String message, String chatId) {
        return fitApp.doChatByStream(message, chatId);
    }


    // 使用 Spring 原型 Bean 方式
    @GetMapping("/manus/chat")
    public Map<String, Object> manusChat(String message, String chatId) {
        // 从 Spring 容器获取新的 Manus 实例（每次都是新的）
        Manus manus = applicationContext.getBean(Manus.class);

        // 保存到活跃列表，供 resume 接口使用
        activeAgents.put(chatId, manus);

        String result = manus.run(message);

        Map<String, Object> response = new HashMap<>();
        if (result.contains("[PAUSED_FOR_HUMAN_INPUT]")) {
            response.put("status", "waiting_human");
            response.put("question", manus.getPendingHumanQuestion());
            response.put("chatId", chatId);
        } else {
            response.put("status", "completed");
            response.put("result", result);
        }
        return response;
    }


    /**
     * 恢复 Agent 执行，提交人类回复
     */
    @PostMapping("/manus/resume")
    public String resumeAgent(@RequestParam String chatId, @RequestParam String response) {
        Manus manus = activeAgents.get(chatId);
        if (manus == null) {
            return "错误：找不到对应的 Agent 会话";
        }

        try {
            manus.resume(response);
            return "已提交回复，Agent 继续执行";
        } catch (IllegalStateException e) {
            return "错误：" + e.getMessage();
        }
    }

    /**
     * 查询 Agent 状态
     */
    @GetMapping("/manus/status")
    public String getAgentStatus(@RequestParam String chatId) {
        Manus manus = activeAgents.get(chatId);
        if (manus == null) {
            return "NOT_FOUND";
        }
        return manus.getState().name();
    }

}
