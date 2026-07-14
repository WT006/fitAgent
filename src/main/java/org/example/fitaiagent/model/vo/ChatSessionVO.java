package org.example.fitaiagent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康咨询会话列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String chatId;

    private String title;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
