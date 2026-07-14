package org.example.fitaiagent.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitaiagent.agent.Manus;
import org.example.fitaiagent.app.FitApp;
import org.example.fitaiagent.common.BaseResponse;
import org.example.fitaiagent.common.ResultUtils;
import org.example.fitaiagent.constant.UserConstant;
import org.example.fitaiagent.model.entity.User;
import org.example.fitaiagent.model.vo.ChatHistoryMessageVO;
import org.example.fitaiagent.model.vo.ChatSessionVO;
import org.example.fitaiagent.service.UserService;
import org.example.fitaiagent.utils.JwtUtils;
import org.example.fitaiagent.utils.RequestUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
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

    @Resource
    private UserService userService;

    private final ConcurrentHashMap<String, Manus> activeAgents = new ConcurrentHashMap<>();

    @GetMapping("/fitapp/chat/sync")
    public String chat(@RequestParam String message,
                       @RequestParam String chatId,
                       @RequestParam(required = false) String token,
                       HttpServletRequest request) {
        User user = resolveFitAppUser(request, token);
        return fitApp.doChat(user, message, chatId);
    }

    @GetMapping(value = "/fitapp/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String message,
                                   @RequestParam String chatId,
                                   @RequestParam(required = false) String token,
                                   HttpServletRequest request) {
        User user = resolveFitAppUser(request, token);
        return fitApp.doChatByStream(user, message, chatId);
    }

    /**
     * 当前用户的会话列表（需登录 Header）
     */
    @GetMapping("/fitapp/chat/sessions")
    public BaseResponse<List<ChatSessionVO>> chatSessions(HttpServletRequest request) {
        User user = RequestUtils.getLoginUser(request);
        return ResultUtils.success(fitApp.listSessions(user));
    }

    /**
     * 获取健康咨询会话历史（需登录 Header）
     */
    @GetMapping("/fitapp/chat/history")
    public BaseResponse<List<ChatHistoryMessageVO>> chatHistory(@RequestParam String chatId,
                                                                HttpServletRequest request) {
        User user = RequestUtils.getLoginUser(request);
        return ResultUtils.success(fitApp.listHistory(user, chatId));
    }

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        Manus myManus = new Manus(toolcallbacks, dashscopeChatModel);
        return myManus.runStream(message);
    }

    /**
     * EventSource 无法带 Authorization，优先用 query token；否则尝试 Header / request 属性
     */
    private User resolveFitAppUser(HttpServletRequest request, String tokenParam) {
        Object attr = request.getAttribute(UserConstant.LOGIN_USER_KEY);
        if (attr instanceof User user) {
            return user;
        }
        if (StringUtils.hasText(tokenParam)) {
            return userService.getLoginUserByToken(tokenParam);
        }
        try {
            String authorization = request.getHeader(UserConstant.AUTHORIZATION_HEADER);
            String token = JwtUtils.extractToken(authorization);
            return userService.getLoginUserByToken(token);
        } catch (Exception e) {
            return userService.getLoginUserByToken(null);
        }
    }
}
