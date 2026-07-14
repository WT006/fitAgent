-- fitagent 计划与每日任务表
USE fit_agent;

CREATE TABLE IF NOT EXISTS `plan` (
    `id`         BIGINT   NOT NULL COMMENT 'id',
    `userId`     BIGINT   NOT NULL COMMENT '所属用户',
    `startDate`  DATE     NOT NULL COMMENT '计划开始日期',
    `status`     TINYINT  NOT NULL DEFAULT 0 COMMENT '0=进行中, 1=已完成, 2=已作废',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_userId_status` (`userId`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='30天健康计划';

CREATE TABLE IF NOT EXISTS `daily_task` (
    `id`           BIGINT       NOT NULL COMMENT 'id',
    `planId`       BIGINT       NOT NULL COMMENT '所属计划',
    `dayIndex`     INT          NOT NULL COMMENT '第几天（1-30）',
    `taskDate`     DATE         NOT NULL COMMENT '任务日期',
    `title`        VARCHAR(200) NOT NULL COMMENT '任务标题',
    `type`         VARCHAR(20)  NOT NULL COMMENT 'SPORT/DIET/REST/HABIT',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0=未完成, 1=已完成',
    `completedAt`  DATETIME     NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    KEY `idx_planId_taskDate` (`planId`, `taskDate`),
    KEY `idx_planId_dayIndex` (`planId`, `dayIndex`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='每日打卡任务';
