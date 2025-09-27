package com.strawberry.irrigation.module_auth.security;

import com.strawberry.irrigation.module_auth.utils.JwtUtil;
import com.strawberry.irrigation.module_user.entity.User;
import com.strawberry.irrigation.module_user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 * 
 * 通俗解释：这就像"门卫"，检查每个请求是否带有有效的"通行证"（JWT令牌）
 * 专业解释：Spring Security过滤器，拦截HTTP请求，验证JWT令牌并设置认证上下文
 * 项目中怎么用：每个请求都会经过这个过滤器，验证通过后才能访问受保护的资源
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    /**
     * 不需要JWT认证的路径列表
     * 
     * 为什么这么做：某些接口（如登录、注册）不需要认证，避免无限循环
     * 怎么做：定义白名单路径，这些路径直接跳过JWT验证
     * 注意点：路径要与SecurityConfig中的配置保持一致
     */
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/api/auth/register",
        "/api/auth/login", 
        "/api/auth/refresh",
        "/api/system/",
        "/error",
        "/swagger-ui/",
        "/v3/api-docs/",
        "/static/",
        "/public/"
    );
    
    /**
     * 过滤器核心逻辑
     * 
     * 为什么这么做：在请求到达Controller之前，先验证用户身份，确保安全性
     * 怎么做：从Authorization头提取token → 验证token → 设置Spring Security上下文
     * 注意点：只处理一次请求(OncePerRequestFilter)，避免重复验证
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        log.debug("JWT过滤器处理请求: {}", requestPath);
        
        // 检查是否为排除路径
        if (shouldSkipFilter(requestPath)) {
            log.debug("跳过JWT验证，路径: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 从请求头中提取JWT令牌
            String token = extractTokenFromRequest(request);
            
            if (StringUtils.hasText(token)) {
                // 从令牌中获取用户名
                String username = jwtUtil.getUsernameFromToken(token);
                log.debug("从JWT令牌中提取用户名: {}", username);
                
                // 验证令牌
                if (jwtUtil.validateToken(token, username)) {
                
                // 如果当前没有认证信息，则设置认证
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    setAuthenticationContext(request, username);
                }
                } else {
                    log.debug("JWT令牌验证失败");
                }
            } else {
                log.debug("JWT令牌无效或不存在");
            }
        } catch (Exception e) {
            log.error("JWT认证过程中发生错误: {}", e.getMessage());
            // 不抛出异常，让请求继续，由Spring Security处理未认证的情况
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 检查请求路径是否应该跳过JWT验证
     * 
     * @param requestPath 请求路径
     * @return true表示跳过验证，false表示需要验证
     */
    private boolean shouldSkipFilter(String requestPath) {
        return EXCLUDED_PATHS.stream()
                .anyMatch(path -> requestPath.startsWith(path));
    }
    
    /**
     * 从HTTP请求中提取JWT令牌
     * 
     * @param request HTTP请求对象
     * @return JWT令牌字符串，如果不存在则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // 去掉"Bearer "前缀
        }
        
        return null;
    }
    
    /**
     * 设置Spring Security认证上下文
     * 
     * @param request HTTP请求对象
     * @param username 用户名
     */
    private void setAuthenticationContext(HttpServletRequest request, String username) {
        try {
            // 从数据库加载用户信息
            User user = userService.getUserEntityByUsername(username);
            
            // 创建自定义UserDetails对象
            CustomUserDetails userDetails = new CustomUserDetails(user);
            
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    userDetails.getAuthorities()
                );
            
            // 设置请求详情
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // 将认证信息设置到Spring Security上下文中
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.debug("成功设置用户认证上下文: {}, 权限: {}", 
                     username, userDetails.getAuthorities());
            
        } catch (Exception e) {
            log.error("设置认证上下文失败，用户名: {}, 错误: {}", username, e.getMessage());
            throw new RuntimeException("认证失败", e);
        }
    }
}