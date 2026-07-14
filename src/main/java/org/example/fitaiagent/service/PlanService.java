package org.example.fitaiagent.service;

import com.mybatisflex.core.service.IService;
import org.example.fitaiagent.model.dto.PlanConfirmRequest;
import org.example.fitaiagent.model.entity.Plan;
import org.example.fitaiagent.model.entity.User;
import org.example.fitaiagent.model.vo.DailyTaskVO;
import org.example.fitaiagent.model.vo.PlanDayTasksVO;
import org.example.fitaiagent.model.vo.PlanProgressVO;

import java.time.LocalDate;

/**
 * 计划服务
 */
public interface PlanService extends IService<Plan> {

    /**
     * 获取当前用户进行中的计划进度
     */
    PlanProgressVO getCurrentProgress(User user);

    /**
     * 获取指定日期的任务列表
     */
    PlanDayTasksVO getTasksByDate(User user, LocalDate date);

    /**
     * 切换任务完成状态
     */
    DailyTaskVO toggleTask(User user, Long taskId);

    /**
     * 确认并写入计划（会作废旧计划）
     */
    PlanProgressVO confirmPlan(User user, PlanConfirmRequest request);

    /**
     * 作废旧计划并清除任务
     */
    void voidActivePlan(Long userId);

    /**
     * 获取用户进行中的计划
     */
    Plan getActivePlan(Long userId);
}
