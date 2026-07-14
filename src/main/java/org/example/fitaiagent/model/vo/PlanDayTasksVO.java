package org.example.fitaiagent.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 某日任务列表视图
 */
@Data
public class PlanDayTasksVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate date;

    private Integer dayIndex;

    private Boolean canToggle;

    private List<DailyTaskVO> tasks;
}
