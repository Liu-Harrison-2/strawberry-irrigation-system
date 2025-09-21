package com.strawberry.irrigation.module_user.controller;


import com.strawberry.irrigation.common.response.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查控制器
 * 用于验证基础架构是否正常工作
 */
@RestController
@RequestMapping("/api/system")
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "草莓栽培智能灌溉系统后端");
        data.put("version", "0.0.1-SNAPSHOT");

        return Result.success(data);
    }

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("后端服务运行正常！");
    }
}