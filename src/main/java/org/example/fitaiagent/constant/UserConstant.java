package org.example.fitaiagent.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 默认角色：普通用户
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 密码加密盐值
     */
    String PASSWORD_SALT = "fitagent";

    /**
     * JWT 密钥（生产环境请替换）
     */
    String JWT_SECRET = "fitagent_jwt_secret_key_2026";

    /**
     * JWT 有效期：7 天（毫秒）
     */
    long JWT_EXPIRE_MS = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 请求头 Token 名称
     */
    String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Token 前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * 当前登录用户 request attribute key
     */
    String LOGIN_USER_KEY = "loginUser";
}
