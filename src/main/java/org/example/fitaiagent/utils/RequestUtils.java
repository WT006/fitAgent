package org.example.fitaiagent.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.example.fitaiagent.constant.UserConstant;
import org.example.fitaiagent.exception.BusinessException;
import org.example.fitaiagent.exception.ErrorCode;
import org.example.fitaiagent.model.entity.User;

/**
 * 请求工具类
 */
public final class RequestUtils {

    private RequestUtils() {
    }

    public static User getLoginUser(HttpServletRequest request) {
        Object loginUser = request.getAttribute(UserConstant.LOGIN_USER_KEY);
        if (loginUser instanceof User user) {
            return user;
        }
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }
}
