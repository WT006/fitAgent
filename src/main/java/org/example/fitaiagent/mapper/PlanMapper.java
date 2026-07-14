package org.example.fitaiagent.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.fitaiagent.model.entity.Plan;

/**
 * 计划 映射层
 */
@Mapper
public interface PlanMapper extends BaseMapper<Plan> {
}
