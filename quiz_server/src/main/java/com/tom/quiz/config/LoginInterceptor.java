package com.tom.quiz.config;

import com.tom.quiz.mapper.UserMapper;
import com.tom.quiz.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token 拦截器 — 从 Header 中取 token，验证并注入用户信息
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // 不需要登录的路径
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.startsWith("/api/health") || path.startsWith("/uploads/")) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        String token = auth.substring(7);
        User user = userMapper.findByToken(token);
        if (user == null || user.getStatus() == 0) {
            response.setStatus(401);
            return false;
        }

        // 注入用户信息到 request attribute
        request.setAttribute("currentUser", user);
        return true;
    }
}
