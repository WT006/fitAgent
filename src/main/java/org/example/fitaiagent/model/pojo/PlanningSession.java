package org.example.fitaiagent.model.pojo;

import lombok.Data;
import org.example.fitaiagent.model.vo.PlanPreviewVO;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 规划会话（存 Redis）
 */
@Data
public class PlanningSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 已收集的回答数量（0-4）
     */
    private int answeredCount;

    private List<String> answers = new ArrayList<>();

    private PlanPreviewVO preview;
}
