package com.tom.quiz.controller;

import com.tom.quiz.common.Result;
import com.tom.quiz.models.Question;
import com.tom.quiz.models.User;
import com.tom.quiz.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // ===== 教师：添加题目 =====
    @PostMapping
    public Result<Question> add(HttpServletRequest req, @RequestBody Question q) {
        User user = (User) req.getAttribute("currentUser");
        return Result.ok(questionService.addQuestion(user.getId(), q));
    }

    // ===== 教师：更新题目 =====
    @PutMapping("/{id}")
    public Result<Question> update(HttpServletRequest req, @PathVariable Long id, @RequestBody Question q) {
        User user = (User) req.getAttribute("currentUser");
        q.setId(id);
        return Result.ok(questionService.updateQuestion(user.getId(), q));
    }

    // ===== 教师：删除题目 =====
    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest req, @PathVariable Long id) {
        User user = (User) req.getAttribute("currentUser");
        questionService.deleteQuestion(user.getId(), id);
        return Result.ok();
    }

    // ===== 查询试题集的题目 =====
    @GetMapping("/set/{setId}")
    public Result<List<Question>> list(@PathVariable Long setId,
                                       @RequestParam(required = false) String type,
                                       @RequestParam(required = false) String difficulty) {
        return Result.ok(questionService.getQuestionsBySetId(setId, type, difficulty));
    }

    // ===== 教师：Excel 导入 =====
    @PostMapping("/import/{setId}")
    public Result<Map<String, Object>> importExcel(HttpServletRequest req,
                                                    @PathVariable Long setId,
                                                    @RequestParam("file") MultipartFile file) {
        User user = (User) req.getAttribute("currentUser");
        int count = questionService.importExcel(user.getId(), setId, file);
        return Result.ok(Map.of("imported", count));
    }

    // ===== 教师：Excel 导出 =====
    @GetMapping("/export/{setId}")
    public void exportExcel(@PathVariable Long setId, HttpServletResponse response) {
        try {
            ByteArrayOutputStream bos = questionService.exportExcel(setId);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = URLEncoder.encode("题目导出.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
            OutputStream os = response.getOutputStream();
            os.write(bos.toByteArray());
            os.flush();
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }
}
