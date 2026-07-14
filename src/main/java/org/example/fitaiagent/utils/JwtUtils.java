package org.example.fitaiagent.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.example.fitaiagent.constant.UserConstant;
import org.example.fitaiagent.exception.BusinessException;
import org.example.fitaiagent.exception.ErrorCode;
import org.example.fitaiagent.model.entity.User;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
public final class JwtUtils {

    private JwtUtils() {
    }

    /**
     * 生成 Token
     */
    public static String generateToken(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId());
        payload.put("userRole", user.getUserRole());
        payload.put("exp", DateUtil.offsetMillisecond(new Date(), (int) UserConstant.JWT_EXPIRE_MS));
        return JWTUtil.createToken(payload, UserConstant.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析 Token，返回 userId
     */
    public static Long getUserId(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            if (!jwt.setKey(UserConstant.JWT_SECRET.getBytes(StandardCharsets.UTF_8)).verify()) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 无效");
            }
            Object userId = jwt.getPayload("userId");
            if (userId == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 无效");
            }
            return Long.valueOf(userId.toString());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 无效或已过期");
        }
    }

    /**
     * 从 Authorization 请求头提取 Token
     */
    public static String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (authorizationHeader.startsWith(UserConstant.TOKEN_PREFIX)) {
            return authorizationHeader.substring(UserConstant.TOKEN_PREFIX.length()).trim();
        }
        return authorizationHeader.trim();
    }
}
