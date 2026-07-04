package com.tom.quiz.controller;

import com.tom.quiz.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查 / 测试接口
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Result<String> health() {
        return Result.ok("quiz_server is running ✅");
    }
}
