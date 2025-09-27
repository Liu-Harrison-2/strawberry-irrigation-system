package com.strawberry.irrigation.module_auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strawberry.irrigation.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证入口点
 * 
 * 通俗解释：当用户没有登录就访问需要登录的页面时，这个类负责告诉用户"请先登录"
 * 专业解释：Spring Security认证入口点，处理未认证用户访问受保护资源的情况
 * 项目中怎么用：当JWT验证失败或用户未提供令牌时，返回统一的错误响应
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理认证异常
     * 
     * 为什么这么做：提供统一的未认证响应格式，符合前端API约定
     * 怎么做：返回JSON格式的错误信息，状态码401
     * 注意点：不要暴露敏感的系统信息，只返回必要的错误提示
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象  
     * @param authException 认证异常
     */
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("未认证访问受保护资源: {} {}, 错误: {}", 
                request.getMethod(), request.getRequestURI(), authException.getMessage());
        
        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 创建统一的错误响应
        Result<Object> result = Result.fail(401, "认证失败，请先登录");
        
        // 将响应对象转换为JSON并写入响应体
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}