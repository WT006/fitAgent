package org.example.fitaiagent.controller;

import jakarta.annotation.Resource;
import org.example.fitaiagent.agent.Manus;
import org.example.fitaiagent.app.FitApp;
import org.example.fitaiagent.common.BaseResponse;
import org.example.fitaiagent.common.ResultUtils;
import org.example.fitaiagent.model.vo.ChatHistoryMessageVO;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;
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

    /**
     * 获取健康咨询会话历史（刷新页面后恢复）
     */
    @GetMapping("/fitapp/chat/history")
    public BaseResponse<List<ChatHistoryMessageVO>> chatHistory(@RequestParam String chatId) {
        return ResultUtils.success(fitApp.listHistory(chatId));
    }


    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        Manus myManus = new Manus(toolcallbacks, dashscopeChatModel);
        return myManus.runStream(message);
    }
}
