package org.example.fitaiagent.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 计划进度视图
 */
@Data
public class PlanProgressVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long planId;

    private LocalDate startDate;

    private Integer currentDay;

    private Integer totalDays;

    private Integer progressPercent;

    private Boolean hasPlan;
}
