package com.strawberry.irrigation.module_auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Spring Security安全配置类
 * 
 * 通俗解释：这就像给房子装门锁和监控系统，决定谁能进来、能去哪个房间
 * 专业解释：配置Spring Security的认证、授权、CORS、会话管理等安全策略
 * 项目中怎么用：定义哪些接口需要登录、哪些需要特定权限、如何处理跨域等
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // 启用方法级别的安全注解
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    
    /**
     * 配置安全过滤器链
     * 
     * 为什么这么做：定义整个应用的安全策略，包括哪些URL需要认证、权限等
     * 怎么做：使用HttpSecurity配置各种安全规则
     * 注意点：规则的顺序很重要，更具体的规则要放在前面
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（因为使用JWT，不需要CSRF保护）
            .csrf().disable()
            
            // 启用CORS支持
            .cors()
            .and()
            
            // 配置异常处理
            .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 未认证处理
                .accessDeniedHandler(jwtAccessDeniedHandler)            // 权限不足处理
            .and()
            
            // 配置会话管理：无状态（不创建HttpSession）
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            
            // 配置URL访问权限
            .authorizeHttpRequests(authz -> authz
                // 公开接口：不需要认证
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login", 
                    "/api/auth/refresh"
                ).permitAll()
                
                // 系统接口：不需要认证
                .requestMatchers("/api/system/**").permitAll()
                
                // 静态资源：不需要认证
                .requestMatchers(
                    "/error",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/static/**",
                    "/public/**"
                ).permitAll()
                
                // 用户管理接口：需要管理员权限
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN") 
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                
                // 设备管理接口：需要管理员或技术员权限
                .requestMatchers(HttpMethod.POST, "/api/devices").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers(HttpMethod.PUT, "/api/devices/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/devices/**").hasRole("ADMIN")
                
                // 其他所有接口：需要认证
                .anyRequest().authenticated()
            )
            
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * 密码编码器Bean
     * 
     * 为什么这么做：提供统一的密码加密方式，确保密码安全存储
     * 怎么做：使用BCrypt算法，自动加盐，每次加密结果都不同
     * 注意点：BCrypt是单向加密，只能验证不能解密，安全性很高
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}