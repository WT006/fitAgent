package org.example.fitaiagent.chatMemory;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.mapper.ChatMessageMapper;
import org.example.fitaiagent.mapper.ChatSessionMapper;
import org.example.fitaiagent.model.entity.ChatMessage;
import org.example.fitaiagent.model.entity.ChatSession;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于 MySQL 持久化的对话记忆（conversationId = userId:chatId）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DBChatMemory implements ChatMemory {

    private static final int DEFAULT_WINDOW_SIZE = 20;

    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMapper chatSessionMapper;

    /**
     * 构建按用户隔离的记忆键
     */
    public static String memoryKey(Long userId, String chatId) {
        return userId + ":" + chatId;
    }

    public static Long parseUserId(String conversationId) {
        String[] parts = splitKey(conversationId);
        return parts == null ? null : Long.valueOf(parts[0]);
    }

    public static String parseChatId(String conversationId) {
        String[] parts = splitKey(conversationId);
        return parts == null ? conversationId : parts[1];
    }

    private static String[] splitKey(String conversationId) {
        if (!StringUtils.hasText(conversationId) || !conversationId.contains(":")) {
            return null;
        }
        String[] parts = conversationId.split(":", 2);
        if (parts.length != 2 || !StringUtils.hasText(parts[0]) || !StringUtils.hasText(parts[1])) {
            return null;
        }
        return parts;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        Long userId = parseUserId(conversationId);
        String chatId = parseChatId(conversationId);
        if (userId == null || !StringUtils.hasText(chatId)) {
            log.warn("Invalid conversationId for DBChatMemory.add: {}", conversationId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<ChatMessage> entities = new ArrayList<>(messages.size());
        String firstUserText = null;
        for (Message msg : messages) {
            String[] parts = msg.getText().split("</think>");
            String text = parts.length == 2 ? parts[1].trim() : parts[0];
            entities.add(ChatMessage.builder()
                    .userId(userId)
                    .chatId(chatId)
                    .type(msg.getMessageType().getValue())
                    .text(text)
                    .createTime(now)
                    .build());
            if (firstUserText == null && MessageType.USER.getValue().equals(msg.getMessageType().getValue())) {
                firstUserText = text;
            }
        }
        chatMessageMapper.insertBatch(entities);
        upsertSession(userId, chatId, firstUserText, now);
    }

    private void upsertSession(Long userId, String chatId, String firstUserText, LocalDateTime now) {
        ChatSession existing = chatSessionMapper.selectOneByQuery(
                QueryWrapper.create().eq("chatId", chatId).eq("userId", userId).limit(1)
        );
        if (existing == null) {
            String title = buildTitle(firstUserText);
            chatSessionMapper.insert(ChatSession.builder()
                    .chatId(chatId)
                    .userId(userId)
                    .title(title)
                    .createTime(now)
                    .updateTime(now)
                    .build());
            return;
        }
        existing.setUpdateTime(now);
        if ((!StringUtils.hasText(existing.getTitle()) || "新对话".equals(existing.getTitle()))
                && StringUtils.hasText(firstUserText)) {
            existing.setTitle(buildTitle(firstUserText));
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

    @Override
    public List<Message> get(String conversationId) {
        return get(conversationId, DEFAULT_WINDOW_SIZE);
    }

    public List<Message> get(String conversationId, int lastN) {
        Long userId = parseUserId(conversationId);
        String chatId = parseChatId(conversationId);
        List<ChatMessage> rows = listEntities(userId, chatId, lastN);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Message> result = new ArrayList<>(rows.size());
        for (ChatMessage row : rows) {
            Message message = toMessage(row);
            if (message != null) {
                result.add(message);
            }
        }
        return result;
    }

    /**
     * 按用户 + 会话读取消息（时间正序）
     */
    public List<ChatMessage> listEntities(Long userId, String chatId, int lastN) {
        if (userId == null || !StringUtils.hasText(chatId)) {
            return Collections.emptyList();
        }
        List<ChatMessage> rows = chatMessageMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("userId", userId)
                        .eq("chatId", chatId)
                        .orderBy("id", false)
                        .limit(lastN)
        );
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.reverse(rows);
        return rows;
    }

    @Override
    public void clear(String conversationId) {
        Long userId = parseUserId(conversationId);
        String chatId = parseChatId(conversationId);
        if (userId == null || !StringUtils.hasText(chatId)) {
            return;
        }
        chatMessageMapper.deleteByQuery(
                QueryWrapper.create().eq("userId", userId).eq("chatId", chatId)
        );
    }

    private Message toMessage(ChatMessage row) {
        String type = row.getType();
        String text = row.getText();
        if (MessageType.USER.getValue().equals(type)) {
            return new UserMessage(text);
        }
        if (MessageType.ASSISTANT.getValue().equals(type)) {
            return new AssistantMessage(text);
        }
        if (MessageType.SYSTEM.getValue().equals(type)) {
            return new SystemMessage(text);
        }
        log.warn("Unknown chat message type: {}, chatId={}", type, row.getChatId());
        return null;
    }
}
