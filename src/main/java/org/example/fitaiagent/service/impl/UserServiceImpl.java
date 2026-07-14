package org.example.fitaiagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitaiagent.constant.UserConstant;
import org.example.fitaiagent.exception.ErrorCode;
import org.example.fitaiagent.exception.ThrowUtils;
import org.example.fitaiagent.mapper.UserMapper;
import org.example.fitaiagent.model.entity.User;
import org.example.fitaiagent.model.dto.UserLoginRequest;
import org.example.fitaiagent.model.dto.UserQueryRequest;
import org.example.fitaiagent.model.dto.UserRegisterRequest;
import org.example.fitaiagent.model.vo.LoginUserVO;
import org.example.fitaiagent.model.vo.UserVO;
import org.example.fitaiagent.service.UserService;
import org.example.fitaiagent.utils.JwtUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 服务层实现。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userName = userRegisterRequest.getUserName();

        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 16,
                ErrorCode.PARAMS_ERROR, "账号长度为 4-16 位");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "密码长度不能少于 8 位");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        synchronized (userAccount.intern()) {
            long count = this.count(QueryWrapper.create().eq("userAccount", userAccount));
            ThrowUtils.throwIf(count > 0, ErrorCode.OPERATION_ERROR, "账号已被注册");

            String encryptPassword = getEncryptPassword(userPassword);
            LocalDateTime now = LocalDateTime.now();
            User user = User.builder()
                    .userAccount(userAccount)
                    .userPassword(encryptPassword)
                    .userName(StrUtil.isBlank(userName) ? userAccount : userName)
                    .userRole(UserConstant.DEFAULT_ROLE)
                    .editTime(now)
                    .createTime(now)
                    .updateTime(now)
                    .isDelete(0)
                    .build();
            boolean saved = this.save(user);
            ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "注册失败");
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码错误");

        String encryptPassword = getEncryptPassword(userPassword);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userAccount", userAccount)
                .eq("userPassword", encryptPassword);
        User user = this.getOne(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "账号或密码错误");

        String token = JwtUtils.generateToken(user);
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setToken(token);
        loginUserVO.setUserVO(getUserVO(user));
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        String authorization = request.getHeader(UserConstant.AUTHORIZATION_HEADER);
        String token = JwtUtils.extractToken(authorization);
        Long userId = JwtUtils.getUserId(token);

        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // JWT 无状态，客户端删除 Token 即可
        return true;
    }

    @Override
    public Page<UserVO> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        checkAdminAuth(request);

        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 50, ErrorCode.PARAMS_ERROR, "每页最多 50 条");

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id, id != null)
                .like("userAccount", userAccount, StrUtil.isNotBlank(userAccount))
                .like("userName", userName, StrUtil.isNotBlank(userName))
                .eq("userRole", userRole, StrUtil.isNotBlank(userRole));

        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equals(sortOrder);
            queryWrapper.orderBy(sortField, isAsc);
        } else {
            queryWrapper.orderBy("createTime", false);
        }

        Page<User> userPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<UserVO> userVOPage = new Page<>(userPage.getPageNumber(), userPage.getPageSize(), userPage.getTotalRow());
        List<UserVO> userVOList = userPage.getRecords().stream().map(this::getUserVO).toList();
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    @Override
    public boolean deleteUser(Long id, HttpServletRequest request) {
        checkAdminAuth(request);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户 id 无效");

        User loginUser = getLoginUser(request);
        ThrowUtils.throwIf(loginUser.getId().equals(id), ErrorCode.OPERATION_ERROR, "不能删除自己的账号");

        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        return this.removeById(id);
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public void checkAdminAuth(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        ThrowUtils.throwIf(!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
    }

    /**
     * 密码加密
     */
    private String getEncryptPassword(String userPassword) {
        return DigestUtil.md5Hex(UserConstant.PASSWORD_SALT + userPassword);
    }
}
