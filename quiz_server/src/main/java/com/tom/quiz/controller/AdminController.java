package com.tom.quiz.controller;

import com.tom.quiz.common.Result;
import com.tom.quiz.mapper.*;
import com.tom.quiz.models.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QuestionSetMapper questionSetMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private AnnouncementMapper announcementMapper;
    @Autowired
    private AuditLogMapper auditLogMapper;
    @Autowired
    private PresetStudentMapper presetStudentMapper;

    // ===== 统计概览 =====
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalUsers", userMapper.count());
        map.put("totalSets", questionSetMapper.count());
        return Result.ok(map);
    }

    // ===== 用户管理 =====
    @GetMapping("/users")
    public Result<List<User>> users(@RequestParam(defaultValue = "student") String role) {
        List<User> users = userMapper.findByRole(role);
        users.forEach(u -> u.setPassword(null)); // 脱敏
        return Result.ok(users);
    }

    @PutMapping("/users/{id}/role")
    public Result<Void> changeRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userMapper.updateRole(id, body.get("role"));
        return Result.ok();
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        userMapper.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    // ===== 全部试题集 =====
    @GetMapping("/sets")
    public Result<List<QuestionSet>> allSets() {
        return Result.ok(questionSetMapper.findAll());
    }

    // ===== 公告管理 =====
    @GetMapping("/announcements")
    public Result<List<Announcement>> announcements() {
        return Result.ok(announcementMapper.findAll());
    }

    @PostMapping("/announcements")
    public Result<Announcement> createAnnouncement(HttpServletRequest req, @RequestBody Announcement a) {
        User user = (User) req.getAttribute("currentUser");
        a.setPublisherId(user.getId());
        announcementMapper.insert(a);
        return Result.ok(a);
    }

    @PutMapping("/announcements/{id}")
    public Result<Void> updateAnnouncement(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        announcementMapper.updateStatus(id, body.get("isActive"));
        return Result.ok();
    }

    // ===== 学生端公告 =====
    @GetMapping("/announcements/active")
    public Result<List<Announcement>> activeAnnouncements() {
        return Result.ok(announcementMapper.findActive());
    }

    // ===== 审计日志 =====
    @GetMapping("/logs")
    public Result<List<AuditLog>> logs(@RequestParam(defaultValue = "100") int limit) {
        return Result.ok(auditLogMapper.findRecent(limit));
    }
}
