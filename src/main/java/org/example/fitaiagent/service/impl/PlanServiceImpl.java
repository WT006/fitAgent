package org.example.fitaiagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.example.fitaiagent.constant.PlanConstant;
import org.example.fitaiagent.exception.ErrorCode;
import org.example.fitaiagent.exception.ThrowUtils;
import org.example.fitaiagent.mapper.DailyTaskMapper;
import org.example.fitaiagent.mapper.PlanMapper;
import org.example.fitaiagent.model.dto.PlanConfirmRequest;
import org.example.fitaiagent.model.entity.DailyTask;
import org.example.fitaiagent.model.entity.Plan;
import org.example.fitaiagent.model.entity.User;
import org.example.fitaiagent.model.vo.DailyTaskVO;
import org.example.fitaiagent.model.vo.PlanDayTasksVO;
import org.example.fitaiagent.model.vo.PlanProgressVO;
import org.example.fitaiagent.service.PlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 计划服务实现
 */
@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {

    private static final Set<String> VALID_TYPES = Set.of(
            PlanConstant.TYPE_SPORT,
            PlanConstant.TYPE_DIET,
            PlanConstant.TYPE_REST,
            PlanConstant.TYPE_HABIT
    );

    private final DailyTaskMapper dailyTaskMapper;

    public PlanServiceImpl(DailyTaskMapper dailyTaskMapper) {
        this.dailyTaskMapper = dailyTaskMapper;
    }

    @Override
    public PlanProgressVO getCurrentProgress(User user) {
        Plan plan = getActivePlan(user.getId());
        PlanProgressVO vo = new PlanProgressVO();
        vo.setTotalDays(PlanConstant.PLAN_DAYS);
        if (plan == null) {
            vo.setHasPlan(false);
            vo.setCurrentDay(0);
            vo.setProgressPercent(0);
            return vo;
        }
        int currentDay = calcCurrentDay(plan.getStartDate());
        vo.setHasPlan(true);
        vo.setPlanId(plan.getId());
        vo.setStartDate(plan.getStartDate());
        vo.setCurrentDay(currentDay);
        vo.setProgressPercent(Math.min(100, currentDay * 100 / PlanConstant.PLAN_DAYS));
        return vo;
    }

    @Override
    public PlanDayTasksVO getTasksByDate(User user, LocalDate date) {
        ThrowUtils.throwIf(date == null, ErrorCode.PARAMS_ERROR, "日期不能为空");
        Plan plan = getActivePlan(user.getId());
        ThrowUtils.throwIf(plan == null, ErrorCode.NOT_FOUND_ERROR, "暂无进行中的计划");

        LocalDate endDate = plan.getStartDate().plusDays(PlanConstant.PLAN_DAYS - 1L);
        ThrowUtils.throwIf(date.isBefore(plan.getStartDate()) || date.isAfter(endDate),
                ErrorCode.PARAMS_ERROR, "日期不在计划范围内");

        List<DailyTask> tasks = dailyTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("planId", plan.getId())
                        .eq("taskDate", date)
                        .orderBy("id", true)
        );

        int dayIndex = (int) ChronoUnit.DAYS.between(plan.getStartDate(), date) + 1;
        PlanDayTasksVO vo = new PlanDayTasksVO();
        vo.setDate(date);
        vo.setDayIndex(dayIndex);
        vo.setCanToggle(!date.isAfter(LocalDate.now()));
        vo.setTasks(tasks.stream().map(this::toTaskVO).toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyTaskVO toggleTask(User user, Long taskId) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR);
        Plan plan = getActivePlan(user.getId());
        ThrowUtils.throwIf(plan == null, ErrorCode.NOT_FOUND_ERROR, "暂无进行中的计划");

        DailyTask task = dailyTaskMapper.selectOneById(taskId);
        ThrowUtils.throwIf(task == null || !plan.getId().equals(task.getPlanId()),
                ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        ThrowUtils.throwIf(task.getTaskDate().isAfter(LocalDate.now()),
                ErrorCode.OPERATION_ERROR, "未来日期不可提前打勾");

        if (task.getStatus() == PlanConstant.TASK_STATUS_DONE) {
            task.setStatus(PlanConstant.TASK_STATUS_UNDONE);
            task.setCompletedAt(null);
        } else {
            task.setStatus(PlanConstant.TASK_STATUS_DONE);
            task.setCompletedAt(LocalDateTime.now());
        }
        dailyTaskMapper.update(task);
        return toTaskVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanProgressVO confirmPlan(User user, PlanConfirmRequest request) {
        ThrowUtils.throwIf(request == null || request.getDays() == null || request.getDays().isEmpty(),
                ErrorCode.PARAMS_ERROR, "计划内容不能为空");
        ThrowUtils.throwIf(request.getDays().size() != PlanConstant.PLAN_DAYS,
                ErrorCode.PARAMS_ERROR, "计划必须包含 30 天");

        voidActivePlan(user.getId());

        LocalDate startDate = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        Plan plan = Plan.builder()
                .userId(user.getId())
                .startDate(startDate)
                .status(PlanConstant.STATUS_ACTIVE)
                .createTime(now)
                .build();
        boolean saved = this.save(plan);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "创建计划失败");

        List<DailyTask> taskList = new ArrayList<>();
        for (PlanConfirmRequest.PlanDayDTO day : request.getDays()) {
            ThrowUtils.throwIf(day.getDayIndex() == null || day.getTasks() == null || day.getTasks().isEmpty(),
                    ErrorCode.PARAMS_ERROR, "每日任务不能为空");
            LocalDate taskDate = startDate.plusDays(day.getDayIndex() - 1L);
            for (PlanConfirmRequest.PlanTaskDTO taskDto : day.getTasks()) {
                ThrowUtils.throwIf(StrUtil.isBlank(taskDto.getTitle()), ErrorCode.PARAMS_ERROR, "任务标题不能为空");
                ThrowUtils.throwIf(!VALID_TYPES.contains(taskDto.getType()), ErrorCode.PARAMS_ERROR, "任务类型无效");
                taskList.add(DailyTask.builder()
                        .planId(plan.getId())
                        .dayIndex(day.getDayIndex())
                        .taskDate(taskDate)
                        .title(taskDto.getTitle().trim())
                        .type(taskDto.getType())
                        .status(PlanConstant.TASK_STATUS_UNDONE)
                        .build());
            }
        }
        dailyTaskMapper.insertBatch(taskList);
        return getCurrentProgress(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidActivePlan(Long userId) {
        Plan plan = getActivePlan(userId);
        if (plan == null) {
            return;
        }
        plan.setStatus(PlanConstant.STATUS_VOIDED);
        this.updateById(plan);
        dailyTaskMapper.deleteByQuery(QueryWrapper.create().eq("planId", plan.getId()));
    }

    @Override
    public Plan getActivePlan(Long userId) {
        return this.getOne(QueryWrapper.create()
                .eq("userId", userId)
                .eq("status", PlanConstant.STATUS_ACTIVE)
                .orderBy("createTime", false)
                .limit(1));
    }

    private int calcCurrentDay(LocalDate startDate) {
        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1;
        return (int) Math.max(1, Math.min(PlanConstant.PLAN_DAYS, days));
    }

    private DailyTaskVO toTaskVO(DailyTask task) {
        DailyTaskVO vo = new DailyTaskVO();
        BeanUtil.copyProperties(task, vo);
        return vo;
    }
}
