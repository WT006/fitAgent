package org.example.fitaiagent.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 确认计划请求
 */
@Data
public class PlanConfirmRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 预览中的每日任务（可修改标题）
     */
    private List<PlanDayDTO> days;

    @Data
    public static class PlanDayDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Integer dayIndex;

        private List<PlanTaskDTO> tasks;
    }

    @Data
    public static class PlanTaskDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String title;

        private String type;
    }
}
