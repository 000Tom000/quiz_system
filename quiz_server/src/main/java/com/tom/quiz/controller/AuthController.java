package com.tom.quiz.controller;

import com.tom.quiz.common.Result;
import com.tom.quiz.dto.LoginRequest;
import com.tom.quiz.dto.RegisterRequest;
import com.tom.quiz.dto.UserVO;
import com.tom.quiz.models.User;
import com.tom.quiz.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // ===== 注册 =====
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterRequest req) {
        return Result.ok(userService.register(req));
    }

    // ===== 密码登录 =====
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginRequest req) {
        return Result.ok(userService.login(req));
    }

    // ===== 发送邮箱验证码 =====
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody Map<String, String> body) {
        userService.sendEmailCode(body.get("email"), body.getOrDefault("purpose", "login"));
        return Result.ok();
    }

    // ===== 重置密码 =====
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
        userService.resetPassword(body.get("email"), body.get("code"), body.get("newPassword"));
        return Result.ok();
    }

    // ===== 当前用户信息 =====
    @GetMapping("/profile")
    public Result<UserVO> profile(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return Result.ok(userService.getProfile(user.getId()));
    }

    // ===== 修改密码 =====
    @PostMapping("/update-password")
    public Result<Void> updatePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        User user = (User) request.getAttribute("currentUser");
        userService.updatePassword(user.getId(), body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }

    // ===== 修改邮箱 =====
    @PostMapping("/update-email")
    public Result<Void> updateEmail(HttpServletRequest request, @RequestBody Map<String, String> body) {
        User user = (User) request.getAttribute("currentUser");
        userService.updateEmail(user.getId(), body.get("email"), body.get("code"));
        return Result.ok();
    }
}
