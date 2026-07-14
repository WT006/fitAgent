package org.example.fitaiagent.service;

import org.example.fitaiagent.model.dto.PlanAiChatRequest;
import org.example.fitaiagent.model.entity.User;
import org.example.fitaiagent.model.vo.PlanAiChatVO;
import org.example.fitaiagent.model.vo.PlanPreviewVO;

/**
 * AI 规划服务
 */
public interface PlanAiService {

    /**
     * 规划模式对话（固定问答 / 生成预览）
     */
    PlanAiChatVO chat(User user, PlanAiChatRequest request);

    /**
     * 获取当前预览
     */
    PlanPreviewVO getPreview(User user);

    /**
     * 重新规划：作废旧计划、清除会话、返回第一题
     */
    PlanAiChatVO replan(User user);

    /**
     * 清除规划会话
     */
    void clearSession(Long userId);
}
