package org.example.fitaiagent.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitaiagent.common.BaseResponse;
import org.example.fitaiagent.common.ResultUtils;
import org.example.fitaiagent.exception.ErrorCode;
import org.example.fitaiagent.exception.ThrowUtils;
import org.example.fitaiagent.model.dto.PlanAiChatRequest;
import org.example.fitaiagent.model.dto.PlanConfirmRequest;
import org.example.fitaiagent.model.dto.TaskToggleRequest;
import org.example.fitaiagent.model.entity.User;
import org.example.fitaiagent.model.vo.DailyTaskVO;
import org.example.fitaiagent.model.vo.PlanAiChatVO;
import org.example.fitaiagent.model.vo.PlanDayTasksVO;
import org.example.fitaiagent.model.vo.PlanPreviewVO;
import org.example.fitaiagent.model.vo.PlanProgressVO;
import org.example.fitaiagent.service.PlanAiService;
import org.example.fitaiagent.service.PlanService;
import org.example.fitaiagent.utils.RequestUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 打卡计划控制层
 */
@RestController
@RequestMapping("/plan")
public class PlanController {

    @Resource
    private PlanService planService;

    @Resource
    private PlanAiService planAiService;

    /**
     * 获取当前计划进度
     */
    @GetMapping("/current")
    public BaseResponse<PlanProgressVO> getCurrentPlan(HttpServletRequest request) {
        User user = RequestUtils.getLoginUser(request);
        return ResultUtils.success(planService.getCurrentProgress(user));
    }

    /**
     * 获取指定日期任务列表
     */
    @GetMapping("/tasks")
    public BaseResponse<PlanDayTasksVO> getTasksByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {
        User user = RequestUtils.getLoginUser(request);
        return ResultUtils.success(planService.getTasksByDate(user, date));
    }

    /**
     * 切换任务完成状态
     */
    @PostMapping("/task/toggle")
    public BaseResponse<DailyTaskVO> toggleTask(@RequestBody TaskToggleRequest taskToggleRequest,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(taskToggleRequest == null, ErrorCode.PARAMS_ERROR);
        User user = RequestUtils.getLoginUser(request);
        return ResultUtils.success(planService.toggleTask(user, taskToggleRequest.getTaskId()));
    }

    /**
     * 规划模式对话
     */
    @PostMapping("/ai/chat")
    public BaseResponse<PlanAiChatVO> planAiChat(@RequestBody(required = false) PlanAiChatRequest chatRequest,
                                                 HttpServletRequest request) {
        User user = RequestUtils.getLoginUser(request);
        return ResultUtils.success(planAiService.chat(user, chatRequest));
    }

    /**
     * 获取计划预览
     */
    @GetMapping("/ai/preview")
    public BaseResponse<PlanPreviewVO> getPreview(HttpServletRequest request) {
        User user = RequestUtils.getLoginUser(request);
        return ResultUtils.success(planAiService.getPreview(user));
    }

    /**
     * 确认写入计划
     */
    @PostMapping("/ai/confirm")
    public BaseResponse<PlanProgressVO> confirmPlan(@RequestBody PlanConfirmRequest confirmRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(confirmRequest == null, ErrorCode.PARAMS_ERROR);
        User user = RequestUtils.getLoginUser(request);
        PlanProgressVO progress = planService.confirmPlan(user, confirmRequest);
        planAiService.clearSession(user.getId());
        return ResultUtils.success(progress);
    }

    /**
     * 重新规划
     */
    @PostMapping("/ai/replan")
    public BaseResponse<PlanAiChatVO> replan(HttpServletRequest request) {
        User user = RequestUtils.getLoginUser(request);
        return ResultUtils.success(planAiService.replan(user));
    }
}
