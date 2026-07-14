-- fitagent 用户表
CREATE DATABASE IF NOT EXISTS fitagent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fitagent;

CREATE TABLE IF NOT EXISTS `user` (
    `id`           BIGINT       NOT NULL COMMENT 'id',
    `userAccount`  VARCHAR(256) NOT NULL COMMENT '账号',
    `userPassword` VARCHAR(512) NOT NULL COMMENT '密码',
    `userName`     VARCHAR(256) NULL COMMENT '用户昵称',
    `userRole`     VARCHAR(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
    `editTime`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    `createTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userAccount` (`userAccount`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户';

-- 默认管理员（账号 admin / 密码 12345678）
INSERT INTO `user` (`id`, `userAccount`, `userPassword`, `userName`, `userRole`, `editTime`, `createTime`, `updateTime`, `isDelete`)
VALUES (1, 'admin', 'c65452a948e0921e84a1f85a053c5fea', '管理员', 'admin', NOW(), NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE `userAccount` = `userAccount`;
