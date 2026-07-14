package org.example.fitaiagent.chatMemory;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.mapper.ChatMessageMapper;
import org.example.fitaiagent.model.entity.ChatMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于 MySQL 持久化的对话记忆
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DBChatMemory implements ChatMemory {

    private static final int DEFAULT_WINDOW_SIZE = 20;

    private final ChatMessageMapper chatMessageMapper;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<ChatMessage> entities = new ArrayList<>(messages.size());
        for (Message msg : messages) {
            String[] parts = msg.getText().split("</think>");
            String text = parts.length == 2 ? parts[1].trim() : parts[0];
            entities.add(ChatMessage.builder()
                    .chatId(conversationId)
                    .type(msg.getMessageType().getValue())
                    .text(text)
                    .createTime(now)
                    .build());
        }
        chatMessageMapper.insertBatch(entities);
    }

    @Override
    public List<Message> get(String conversationId) {
        return get(conversationId, DEFAULT_WINDOW_SIZE);
    }

    public List<Message> get(String conversationId, int lastN) {
        List<ChatMessage> rows = listEntities(conversationId, lastN);
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
     * 按会话读取原始消息实体（时间正序），供历史接口与记忆窗口复用
     */
    public List<ChatMessage> listEntities(String conversationId, int lastN) {
        if (conversationId == null || conversationId.isBlank()) {
            return Collections.emptyList();
        }
        List<ChatMessage> rows = chatMessageMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("chatId", conversationId)
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
        chatMessageMapper.deleteByQuery(
                QueryWrapper.create().eq("chatId", conversationId)
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
