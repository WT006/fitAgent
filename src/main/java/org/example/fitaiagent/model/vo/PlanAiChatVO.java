package org.example.fitaiagent.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 规划对话响应
 */
@Data
public class PlanAiChatVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * question / preview / done
     */
    private String phase;

    /**
     * 当前问题序号（1-4），phase=question 时有值
     */
    private Integer questionIndex;

    /**
     * AI 提问内容
     */
    private String question;

    /**
     * 计划预览，phase=preview 时有值
     */
    private PlanPreviewVO preview;
}
