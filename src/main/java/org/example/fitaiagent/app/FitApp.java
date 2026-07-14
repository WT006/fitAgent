package org.example.fitaiagent.app;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.advisor.MyLogAdvisor;
import org.example.fitaiagent.chatMemory.DBChatMemory;
import org.example.fitaiagent.exception.ErrorCode;
import org.example.fitaiagent.exception.ThrowUtils;
import org.example.fitaiagent.mapper.ChatSessionMapper;
import org.example.fitaiagent.model.entity.ChatMessage;
import org.example.fitaiagent.model.entity.ChatSession;
import org.example.fitaiagent.model.entity.User;
import org.example.fitaiagent.model.vo.ChatHistoryMessageVO;
import org.example.fitaiagent.model.vo.ChatSessionVO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class FitApp {

    private static final int HISTORY_LIMIT = 100;

    @Resource
    private VectorStore fitAppVectorStore;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    private final ChatClient chatClient;
    private final DBChatMemory dbChatMemory;

    private static final String SYSTEM_PROMPT = "你是专注运动锻炼领域的专家，擅长帮用户解决各类运动难题，无论是想科学减脂、增肌塑形，还是改善运动习惯、规避运动损伤等。"
            + "围绕运动新手、进阶训练、日常维持三种状态提问："
            + "运动新手：询问不知道选什么运动、坚持不了、动作不标准等困扰；"
            + "进阶训练：询问突破平台期、针对性提升某项目能力、高效训练计划制定的问题；"
            + "日常维持：询问没时间运动、碎片化锻炼、运动与工作生活平衡的难题。"
            + "引导用户详述：运动场景、遇到的具体问题、尝试过的方法及自身运动目标，针对性给出用户专属方案。"
            + "请严格参考完整对话历史；用户已提供的姓名、目标等个人信息要记住并正确引用，不要否认历史中已出现的内容。"
            + "每次回复都会尽量精简，不做多余阐述。";

    public FitApp(ChatModel DashScopeChatModel, DBChatMemory dbChatMemory) {
        this.dbChatMemory = dbChatMemory;
        chatClient = ChatClient.builder(DashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(dbChatMemory).build(),
                        new MyLogAdvisor()
                )
                .build();
    }

    public String doChat(User user, String message, String chatId) {
        String memoryKey = prepareConversation(user, chatId, message);
        ChatResponse response = chatClient
                .prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, memoryKey))
                .advisors(QuestionAnswerAdvisor.builder(fitAppVectorStore).build())
                .toolCallbacks(toolCallbackProvider)
                .user(message)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    public Flux<String> doChatByStream(User user, String message, String chatId) {
        String memoryKey = prepareConversation(user, chatId, message);
        return chatClient
                .prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, memoryKey))
                .advisors(QuestionAnswerAdvisor.builder(fitAppVectorStore).build())
                .toolCallbacks(toolCallbackProvider)
                .user(message)
                .stream()
                .content();
    }

    private String prepareConversation(User user, String chatId, String message) {
        ThrowUtils.throwIf(user == null || user.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!StringUtils.hasText(chatId), ErrorCode.PARAMS_ERROR, "chatId 不能为空");
        ensureSession(user.getId(), chatId, message);
        return DBChatMemory.memoryKey(user.getId(), chatId);
    }

    private void ensureSession(Long userId, String chatId, String message) {
        ChatSession existing = chatSessionMapper.selectOneByQuery(
                QueryWrapper.create().eq("chatId", chatId).limit(1)
        );
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            chatSessionMapper.insert(ChatSession.builder()
                    .chatId(chatId)
                    .userId(userId)
                    .title(buildTitle(message))
                    .createTime(now)
                    .updateTime(now)
                    .build());
            return;
        }
        ThrowUtils.throwIf(!userId.equals(existing.getUserId()),
                ErrorCode.NO_AUTH_ERROR, "无权访问该会话");
        existing.setUpdateTime(now);
        if ((!StringUtils.hasText(existing.getTitle()) || "新对话".equals(existing.getTitle()))
                && StringUtils.hasText(message)) {
            existing.setTitle(buildTitle(message));
        }
        chatSessionMapper.update(existing);
    }

    private String buildTitle(String text) {
        if (!StringUtils.hasText(text)) {
            return "新对话";
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        return compact.length() > 30 ? compact.substring(0, 30) + "…" : compact;
    }

    public List<ChatHistoryMessageVO> listHistory(User user, String chatId) {
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!StringUtils.hasText(chatId), ErrorCode.PARAMS_ERROR, "chatId 不能为空");
        assertSessionOwner(user.getId(), chatId);
        return dbChatMemory.listEntities(user.getId(), chatId, HISTORY_LIMIT).stream()
                .filter(this::isVisibleHistoryMessage)
                .map(this::toHistoryVO)
                .toList();
    }

    public List<ChatSessionVO> listSessions(User user) {
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        List<ChatSession> sessions = chatSessionMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("userId", user.getId())
                        .orderBy("updateTime", false)
                        .limit(50)
        );
        if (sessions == null || sessions.isEmpty()) {
            return List.of();
        }
        return sessions.stream().map(this::toSessionVO).toList();
    }

    private void assertSessionOwner(Long userId, String chatId) {
        ChatSession session = chatSessionMapper.selectOneByQuery(
                QueryWrapper.create().eq("chatId", chatId).limit(1)
        );
        if (session == null) {
            // 允许空会话（新建尚未发言）——只要不是别人占用的 chatId
            return;
        }
        ThrowUtils.throwIf(!userId.equals(session.getUserId()),
                ErrorCode.NO_AUTH_ERROR, "无权访问该会话");
    }

    private boolean isVisibleHistoryMessage(ChatMessage message) {
        String type = message.getType();
        return MessageType.USER.getValue().equals(type)
                || MessageType.ASSISTANT.getValue().equals(type);
    }

    private ChatHistoryMessageVO toHistoryVO(ChatMessage message) {
        return ChatHistoryMessageVO.builder()
                .id(String.valueOf(message.getId()))
                .role(message.getType())
                .content(message.getText())
                .createTime(message.getCreateTime())
                .build();
    }

    private ChatSessionVO toSessionVO(ChatSession session) {
        return ChatSessionVO.builder()
                .chatId(session.getChatId())
                .title(StrUtil.blankToDefault(session.getTitle(), "新对话"))
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .build();
    }
}
