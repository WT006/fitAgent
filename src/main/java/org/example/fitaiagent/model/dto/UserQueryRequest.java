package org.example.fitaiagent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.fitaiagent.common.PageRequest;

import java.io.Serial;

/**
 * 用户分页查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 账号（模糊搜索）
     */
    private String userAccount;

    /**
     * 昵称（模糊搜索）
     */
    private String userName;

    /**
     * 用户角色
     */
    private String userRole;
}
