package org.example.fitaiagent.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitaiagent.model.entity.User;
import org.example.fitaiagent.model.dto.UserLoginRequest;
import org.example.fitaiagent.model.dto.UserQueryRequest;
import org.example.fitaiagent.model.dto.UserRegisterRequest;
import org.example.fitaiagent.model.vo.LoginUserVO;
import org.example.fitaiagent.model.vo.UserVO;

/**
 * 用户 服务层。
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册信息
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录信息
     * @return 登录用户（含 Token）
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销（客户端清除 Token 即可，服务端返回成功）
     *
     * @param request 请求
     * @return 是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 管理员分页查询用户
     *
     * @param userQueryRequest 查询条件
     * @param request          请求（校验管理员权限）
     * @return 分页结果
     */
    Page<UserVO> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request);

    /**
     * 管理员删除用户（逻辑删除）
     *
     * @param id      用户 id
     * @param request 请求（校验管理员权限）
     * @return 是否成功
     */
    boolean deleteUser(Long id, HttpServletRequest request);

    /**
     * 实体转 VO
     */
    UserVO getUserVO(User user);

    /**
     * 校验管理员权限
     */
    void checkAdminAuth(HttpServletRequest request);
}
