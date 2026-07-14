package org.example.fitaiagent.constant;

/**
 * 计划与任务常量
 */
public interface PlanConstant {

    int STATUS_ACTIVE = 0;
    int STATUS_COMPLETED = 1;
    int STATUS_VOIDED = 2;

    int TASK_STATUS_UNDONE = 0;
    int TASK_STATUS_DONE = 1;

    String TYPE_SPORT = "SPORT";
    String TYPE_DIET = "DIET";
    String TYPE_REST = "REST";
    String TYPE_HABIT = "HABIT";

    int PLAN_DAYS = 30;

    String PLANNING_SESSION_KEY_PREFIX = "plan:session:";

    String[] PLANNING_QUESTIONS = {
            "你每天大概有多少时间？",
            "你的目标是什么？（减脂 / 增肌 / 保持习惯）",
            "有没有身体限制？（选填，没有可回复「无」）",
            "需要安排饮食或作息吗？"
    };
}
