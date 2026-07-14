package org.example.fitaiagent.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.fitaiagent.model.entity.User;

/**
 * 用户 映射层。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
