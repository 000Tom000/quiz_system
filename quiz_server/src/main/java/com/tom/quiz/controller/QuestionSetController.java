package com.tom.quiz.controller;

import com.tom.quiz.common.Result;
import com.tom.quiz.models.QuestionSet;
import com.tom.quiz.models.User;
import com.tom.quiz.service.QuestionSetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sets")
public class QuestionSetController {

    @Autowired
    private QuestionSetService qsService;

    // ===== 教师：我的试题集 =====
    @GetMapping("/my")
    public Result<List<QuestionSet>> mySets(HttpServletRequest req) {
        User user = (User) req.getAttribute("currentUser");
        return Result.ok(qsService.getMySets(user.getId()));
    }

    // ===== 教师：创建试题集 =====
    @PostMapping
    public Result<QuestionSet> create(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        User user = (User) req.getAttribute("currentUser");
        QuestionSet qs = parseSet(body);
        @SuppressWarnings("unchecked")
        List<String> classNames = (List<String>) body.get("classNames");
        return Result.ok(qsService.create(user.getId(), qs, classNames));
    }

    // ===== 教师：更新试题集 =====
    @PutMapping("/{id}")
    public Result<QuestionSet> update(HttpServletRequest req, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = (User) req.getAttribute("currentUser");
        QuestionSet qs = parseSet(body);
        qs.setId(id);
        @SuppressWarnings("unchecked")
        List<String> classNames = (List<String>) body.get("classNames");
        return Result.ok(qsService.update(user.getId(), qs, classNames));
    }

    // ===== 教师：删除试题集 =====
    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest req, @PathVariable Long id) {
        User user = (User) req.getAttribute("currentUser");
        qsService.delete(user.getId(), id);
        return Result.ok();
    }

    // ===== 详情 =====
    @GetMapping("/{id}")
    public Result<QuestionSet> detail(@PathVariable Long id) {
        return Result.ok(qsService.getById(id));
    }

    // ===== 学生：可刷的试题集 =====
    @GetMapping("/visible")
    public Result<List<QuestionSet>> visible(HttpServletRequest req) {
        User user = (User) req.getAttribute("currentUser");
        return Result.ok(qsService.getVisibleSets(user.getClassName()));
    }

    // ===== 搜索 =====
    @GetMapping("/search")
    public Result<List<QuestionSet>> search(HttpServletRequest req, @RequestParam String keyword) {
        User user = (User) req.getAttribute("currentUser");
        return Result.ok(qsService.search(user.getClassName(), keyword));
    }

    private QuestionSet parseSet(Map<String, Object> body) {
        QuestionSet qs = new QuestionSet();
        qs.setTitle((String) body.get("title"));
        qs.setDescription((String) body.get("description"));
        qs.setRemark((String) body.get("remark"));
        qs.setIsPublic((Integer) body.getOrDefault("isPublic", 0));
        return qs;
    }
}
