package org.example.fitaiagent.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.constant.PlanConstant;
import org.example.fitaiagent.exception.ErrorCode;
import org.example.fitaiagent.exception.ThrowUtils;
import org.example.fitaiagent.model.dto.PlanAiChatRequest;
import org.example.fitaiagent.model.entity.User;
import org.example.fitaiagent.model.pojo.PlanningSession;
import org.example.fitaiagent.model.vo.PlanAiChatVO;
import org.example.fitaiagent.model.vo.PlanPreviewVO;
import org.example.fitaiagent.service.PlanAiService;
import org.example.fitaiagent.service.PlanService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * AI 规划服务实现
 */
@Service
@Slf4j
public class PlanAiServiceImpl implements PlanAiService {

    private static final Set<String> VALID_TYPES = Set.of(
            PlanConstant.TYPE_SPORT,
            PlanConstant.TYPE_DIET,
            PlanConstant.TYPE_REST,
            PlanConstant.TYPE_HABIT
    );

    private static final long SESSION_TTL_HOURS = 2;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatModel chatModel;
    private final PlanService planService;

    public PlanAiServiceImpl(RedisTemplate<String, Object> redisTemplate,
                             ChatModel chatModel,
                             PlanService planService) {
        this.redisTemplate = redisTemplate;
        this.chatModel = chatModel;
        this.planService = planService;
    }

    @Override
    public PlanAiChatVO chat(User user, PlanAiChatRequest request) {
        PlanningSession session = getSession(user.getId());
        String message = request != null ? request.getMessage() : null;

        if (session == null) {
            session = new PlanningSession();
            if (StrUtil.isNotBlank(message)) {
                session.getAnswers().add(message.trim());
                session.setAnsweredCount(1);
            }
            saveSession(user.getId(), session);
            if (session.getAnsweredCount() == 0) {
                return buildQuestionResponse(1);
            }
            if (session.getAnsweredCount() < PlanConstant.PLANNING_QUESTIONS.length) {
                return buildQuestionResponse(session.getAnsweredCount() + 1);
            }
            PlanPreviewVO preview = generatePreview(session.getAnswers());
            session.setPreview(preview);
            saveSession(user.getId(), session);
            PlanAiChatVO vo = new PlanAiChatVO();
            vo.setPhase("preview");
            vo.setPreview(preview);
            return vo;
        }

        if (session.getPreview() != null) {
            PlanAiChatVO vo = new PlanAiChatVO();
            vo.setPhase("preview");
            vo.setPreview(session.getPreview());
            return vo;
        }

        int answered = session.getAnsweredCount();
        if (answered < PlanConstant.PLANNING_QUESTIONS.length) {
            // 无 message：恢复当前未完成的问题（二次进入 / 刷新）
            if (StrUtil.isBlank(message)) {
                return buildQuestionResponse(answered + 1);
            }
            session.getAnswers().add(message.trim());
            session.setAnsweredCount(answered + 1);
            saveSession(user.getId(), session);

            if (session.getAnsweredCount() < PlanConstant.PLANNING_QUESTIONS.length) {
                return buildQuestionResponse(session.getAnsweredCount() + 1);
            }

            PlanPreviewVO preview = generatePreview(session.getAnswers());
            session.setPreview(preview);
            saveSession(user.getId(), session);

            PlanAiChatVO vo = new PlanAiChatVO();
            vo.setPhase("preview");
            vo.setPreview(preview);
            return vo;
        }

        PlanAiChatVO vo = new PlanAiChatVO();
        vo.setPhase("preview");
        vo.setPreview(session.getPreview());
        return vo;
    }

    @Override
    public PlanPreviewVO getPreview(User user) {
        PlanningSession session = getSession(user.getId());
        ThrowUtils.throwIf(session == null || session.getPreview() == null,
                ErrorCode.NOT_FOUND_ERROR, "暂无计划预览，请先完成问答");
        return session.getPreview();
    }

    @Override
    public PlanAiChatVO replan(User user) {
        planService.voidActivePlan(user.getId());
        clearSession(user.getId());
        PlanningSession session = new PlanningSession();
        saveSession(user.getId(), session);
        return buildQuestionResponse(1);
    }

    @Override
    public void clearSession(Long userId) {
        redisTemplate.delete(sessionKey(userId));
    }

    private PlanAiChatVO buildQuestionResponse(int questionIndex) {
        PlanAiChatVO vo = new PlanAiChatVO();
        vo.setPhase("question");
        vo.setQuestionIndex(questionIndex);
        vo.setQuestion(PlanConstant.PLANNING_QUESTIONS[questionIndex - 1]);
        return vo;
    }

    private PlanPreviewVO generatePreview(List<String> answers) {
        String prompt = """
                你是运动健康规划助手。根据用户回答，生成从今天开始的 30 天简单打卡计划。
                
                用户回答：
                1. 每天可用时间：%s
                2. 目标：%s
                3. 身体限制：%s
                4. 饮食作息需求：%s
                
                请严格只输出 JSON，不要 markdown 代码块，不要其他说明文字：
                {
                  "days": [
                    {
                      "dayIndex": 1,
                      "tasks": [
                        {"title": "任务标题", "type": "SPORT"}
                      ]
                    }
                  ]
                }
                
                要求：
                - 必须输出 30 天，dayIndex 从 1 到 30
                - 每天 3 到 5 条任务
                - type 只能是 SPORT、DIET、REST、HABIT
                - 任务简单可执行，适合新手，标题不超过 30 字
                """.formatted(answers.get(0), answers.get(1), answers.get(2), answers.get(3));

        String content = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
        log.info("AI plan generation response length: {}", content != null ? content.length() : 0);
        return parsePreview(content);
    }

    private PlanPreviewVO parsePreview(String content) {
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.OPERATION_ERROR, "AI 生成计划失败");

        String json = extractJson(content);
        JSONObject root = JSONUtil.parseObj(json);
        JSONArray daysArray = root.getJSONArray("days");
        ThrowUtils.throwIf(daysArray == null || daysArray.isEmpty(),
                ErrorCode.OPERATION_ERROR, "AI 返回格式无效");

        PlanPreviewVO preview = new PlanPreviewVO();
        List<PlanPreviewVO.PreviewDayVO> days = new ArrayList<>();
        for (int i = 0; i < daysArray.size(); i++) {
            JSONObject dayObj = daysArray.getJSONObject(i);
            PlanPreviewVO.PreviewDayVO dayVO = new PlanPreviewVO.PreviewDayVO();
            dayVO.setDayIndex(dayObj.getInt("dayIndex"));

            JSONArray tasksArray = dayObj.getJSONArray("tasks");
            ThrowUtils.throwIf(tasksArray == null || tasksArray.isEmpty(),
                    ErrorCode.OPERATION_ERROR, "AI 返回任务为空");

            List<PlanPreviewVO.PreviewTaskVO> tasks = new ArrayList<>();
            for (int j = 0; j < tasksArray.size(); j++) {
                JSONObject taskObj = tasksArray.getJSONObject(j);
                String title = taskObj.getStr("title");
                String type = taskObj.getStr("type");
                ThrowUtils.throwIf(StrUtil.isBlank(title) || !VALID_TYPES.contains(type),
                        ErrorCode.OPERATION_ERROR, "AI 返回任务格式无效");
                PlanPreviewVO.PreviewTaskVO taskVO = new PlanPreviewVO.PreviewTaskVO();
                taskVO.setTitle(title.trim());
                taskVO.setType(type);
                tasks.add(taskVO);
            }
            dayVO.setTasks(tasks);
            days.add(dayVO);
        }

        ThrowUtils.throwIf(days.size() != PlanConstant.PLAN_DAYS,
                ErrorCode.OPERATION_ERROR, "AI 生成的天数不是 30 天");

        preview.setDays(days);
        return preview;
    }

    private String extractJson(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        ThrowUtils.throwIf(start < 0 || end <= start, ErrorCode.OPERATION_ERROR, "AI 返回格式无效");
        return trimmed.substring(start, end + 1);
    }

    private PlanningSession getSession(Long userId) {
        Object obj = redisTemplate.opsForValue().get(sessionKey(userId));
        if (obj == null) {
            return null;
        }
        return JSONUtil.toBean(JSONUtil.toJsonStr(obj), PlanningSession.class);
    }

    private void saveSession(Long userId, PlanningSession session) {
        redisTemplate.opsForValue().set(sessionKey(userId), session, SESSION_TTL_HOURS, TimeUnit.HOURS);
    }

    private String sessionKey(Long userId) {
        return PlanConstant.PLANNING_SESSION_KEY_PREFIX + userId;
    }
}
