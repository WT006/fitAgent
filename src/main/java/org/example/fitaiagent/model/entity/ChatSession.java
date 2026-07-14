package org.example.fitaiagent.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康咨询会话
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_session")
public class ChatSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    @Column("chatId")
    private String chatId;

    @Column("userId")
    private Long userId;

    @Column("title")
    private String title;

    @Column("createTime")
    private LocalDateTime createTime;

    @Column("updateTime")
    private LocalDateTime updateTime;
}
