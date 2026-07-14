package org.example.fitaiagent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史消息视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * user / assistant
     */
    private String role;

    private String content;

    private LocalDateTime createTime;
}
