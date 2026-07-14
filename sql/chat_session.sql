-- fit_agent 对话记忆按用户隔离
USE fit_agent;

-- 已有表补充 userId（可重复执行）
ALTER TABLE `chat_message`
    ADD COLUMN IF NOT EXISTS `userId` BIGINT NULL COMMENT '所属用户' AFTER `id`;

ALTER TABLE `chat_message`
    ADD INDEX IF NOT EXISTS `idx_userId_chatId` (`userId`, `chatId`);

CREATE TABLE IF NOT EXISTS `chat_session` (
    `id`         BIGINT       NOT NULL COMMENT 'id',
    `chatId`     VARCHAR(64)  NOT NULL COMMENT '会话 id',
    `userId`     BIGINT       NOT NULL COMMENT '所属用户',
    `title`      VARCHAR(200) NULL COMMENT '会话标题（首条用户消息摘要）',
    `createTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chatId` (`chatId`),
    KEY `idx_userId_updateTime` (`userId`, `updateTime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='健康咨询会话';
