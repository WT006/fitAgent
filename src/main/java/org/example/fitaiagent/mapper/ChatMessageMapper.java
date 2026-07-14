package org.example.fitaiagent.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.fitaiagent.model.entity.ChatMessage;

/**
 * 对话记忆消息 映射层
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
