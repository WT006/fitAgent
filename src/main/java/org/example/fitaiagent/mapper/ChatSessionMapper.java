package org.example.fitaiagent.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.fitaiagent.model.entity.ChatSession;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
