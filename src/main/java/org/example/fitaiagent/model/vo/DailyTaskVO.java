package org.example.fitaiagent.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 每日任务视图
 */
@Data
public class DailyTaskVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer dayIndex;

    private String title;

    private String type;

    private Integer status;

    private LocalDateTime completedAt;
}
