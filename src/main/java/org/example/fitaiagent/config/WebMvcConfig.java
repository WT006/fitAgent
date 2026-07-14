package org.example.fitaiagent.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.fitaiagent.constant.UserConstant;
import org.example.fitaiagent.exception.BusinessException;
import org.example.fitaiagent.exception.ErrorCode;
import org.example.fitaiagent.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：登录拦截
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserService userService;

    public WebMvcConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(userService))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        // EventSource 流式接口通过 query token 鉴权
                        "/fitai/fitapp/chat/stream",
                        "/fitai/fitapp/chat/sync",
                        "/fitai/manus/chat",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error"
                );
    }

    /**
     * 登录拦截器：校验 JWT，并将当前用户放入 request
     */
    static class AuthInterceptor implements HandlerInterceptor {

        private final UserService userService;

        AuthInterceptor(UserService userService) {
            this.userService = userService;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }
            try {
                var loginUser = userService.getLoginUser(request);
                request.setAttribute(UserConstant.LOGIN_USER_KEY, loginUser);
                return true;
            } catch (BusinessException e) {
                if (e.getCode() == ErrorCode.NOT_LOGIN_ERROR.getCode()) {
                    throw e;
                }
                throw e;
            }
        }
    }
}
