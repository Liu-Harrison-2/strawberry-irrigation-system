package com.strawberry.irrigation.module_auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strawberry.irrigation.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT访问拒绝处理器
 * 
 * 通俗解释：当用户已经登录但没有权限访问某个功能时，这个类负责告诉用户"权限不足"
 * 专业解释：Spring Security访问拒绝处理器，处理已认证用户访问权限不足的情况
 * 项目中怎么用：当用户尝试访问超出其权限范围的资源时，返回统一的权限不足响应
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理访问拒绝异常
     * 
     * 为什么这么做：提供统一的权限不足响应格式，符合前端API约定
     * 怎么做：返回JSON格式的错误信息，状态码403
     * 注意点：记录访问日志，便于安全审计和问题排查
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param accessDeniedException 访问拒绝异常
     */
    @Override
    public void handle(HttpServletRequest request, 
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.warn("权限不足访问受保护资源: {} {}, 用户: {}, 错误: {}", 
                request.getMethod(), 
                request.getRequestURI(),
                getCurrentUsername(request),
                accessDeniedException.getMessage());
        
        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 创建统一的错误响应
        Result<Object> result = Result.fail(403, "权限不足，无法访问该资源");
        
        // 将响应对象转换为JSON并写入响应体
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    /**
     * 获取当前用户名（用于日志记录）
     * 
     * @param request HTTP请求对象
     * @return 当前用户名，如果无法获取则返回"unknown"
     */
    private String getCurrentUsername(HttpServletRequest request) {
        try {
            // 尝试从请求属性中获取用户名
            Object username = request.getAttribute("username");
            if (username != null) {
                return username.toString();
            }
            
            // 如果请求属性中没有，返回默认值
            return "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}