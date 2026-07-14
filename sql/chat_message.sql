-- fit_agent 对话记忆表
USE fit_agent;

CREATE TABLE IF NOT EXISTS `chat_message` (
    `id`         BIGINT       NOT NULL COMMENT 'id',
    `chatId`     VARCHAR(64)  NOT NULL COMMENT '会话 id',
    `type`       VARCHAR(32)  NOT NULL COMMENT '消息类型：USER/ASSISTANT/SYSTEM',
    `text`       TEXT         NOT NULL COMMENT '消息内容',
    `createTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_chatId_id` (`chatId`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='对话记忆消息';
