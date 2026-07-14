package org.example.fitaiagent.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 规划模式对话请求
 */
@Data
public class PlanAiChatRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户回答，首次进入可留空
     */
    private String message;
}
