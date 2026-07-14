package org.example.fitaiagent.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 计划预览视图
 */
@Data
public class PlanPreviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<PreviewDayVO> days;

    @Data
    public static class PreviewDayVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Integer dayIndex;

        private List<PreviewTaskVO> tasks;
    }

    @Data
    public static class PreviewTaskVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String title;

        private String type;
    }
}
