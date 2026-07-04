package com.tom.quiz.controller;

import com.tom.quiz.common.Result;
import com.tom.quiz.models.ExamRecord;
import com.tom.quiz.models.Question;
import com.tom.quiz.models.User;
import com.tom.quiz.service.PracticeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    @Autowired
    private PracticeService practiceService;

    // ===== 开始刷题 =====
    @GetMapping("/start/{setId}")
    public Result<List<Question>> startPractice(HttpServletRequest req, @PathVariable Long setId,
                                                 @RequestParam(defaultValue = "true") boolean shuffleQuestions,
                                                 @RequestParam(defaultValue = "true") boolean shuffleOptions,
                                                 @RequestParam(required = false) String type) {
        User user = (User) req.getAttribute("currentUser");
        return Result.ok(practiceService.startPractice(user.getId(), setId, shuffleQuestions, shuffleOptions, type));
    }

    // ===== 普通模式：提交单题 =====
    @PostMapping("/submit/{questionId}")
    public Result<Map<String, Object>> submit(HttpServletRequest req, @PathVariable Long questionId,
                                               @RequestBody Map<String, String> body) {
        User user = (User) req.getAttribute("currentUser");
        return Result.ok(practiceService.submitAnswer(user.getId(), questionId, body.get("answer")));
    }

    // ===== 收藏 =====
    @PostMapping("/favorite/{setId}")
    public Result<Void> favorite(HttpServletRequest req, @PathVariable Long setId) {
        User user = (User) req.getAttribute("currentUser");
        practiceService.favorite(user.getId(), setId);
        return Result.ok();
    }

    @DeleteMapping("/favorite/{setId}")
    public Result<Void> unfavorite(HttpServletRequest req, @PathVariable Long setId) {
        User user = (User) req.getAttribute("currentUser");
        practiceService.unfavorite(user.getId(), setId);
        return Result.ok();
    }

    @GetMapping("/favorite/{setId}")
    public Result<Boolean> isFavorited(HttpServletRequest req, @PathVariable Long setId) {
        User user = (User) req.getAttribute("currentUser");
        return Result.ok(practiceService.isFavorited(user.getId(), setId));
    }

    // ===== 错题 =====
    @PostMapping("/wrong/{questionId}")
    public Result<Void> addWrong(HttpServletRequest req, @PathVariable Long questionId,
                                  @RequestBody Map<String, Long> body) {
        User user = (User) req.getAttribute("currentUser");
        practiceService.addToWrong(user.getId(), questionId, body.get("questionSetId"));
        return Result.ok();
    }

    @DeleteMapping("/wrong/{questionId}")
    public Result<Void> removeWrong(HttpServletRequest req, @PathVariable Long questionId) {
        User user = (User) req.getAttribute("currentUser");
        practiceService.removeFromWrong(user.getId(), questionId);
        return Result.ok();
    }

    @GetMapping("/wrong")
    public Result<List<Map<String, Object>>> getWrong(HttpServletRequest req,
                                                       @RequestParam(required = false) Long setId,
                                                       @RequestParam(defaultValue = "true") boolean shuffleQuestions,
                                                       @RequestParam(defaultValue = "true") boolean shuffleOptions) {
        User user = (User) req.getAttribute("currentUser");
        return Result.ok(practiceService.getWrongQuestions(user.getId(), setId, shuffleQuestions, shuffleOptions));
    }

    // ===== 考试模式 =====
    @PostMapping("/exam/start/{setId}")
    public Result<ExamRecord> startExam(HttpServletRequest req, @PathVariable Long setId) {
        User user = (User) req.getAttribute("currentUser");
        // 检查未完成
        ExamRecord unfinished = practiceService.getUnfinishedExam(user.getId(), setId);
        if (unfinished != null) {
            return Result.ok(unfinished);
        }
        return Result.ok(practiceService.startExam(user.getId(), setId));
    }

    @PostMapping("/exam/save/{recordId}")
    public Result<Void> saveExamAnswer(HttpServletRequest req, @PathVariable Long recordId,
                                        @RequestBody Map<String, Object> body) {
        practiceService.saveExamAnswer(recordId,
                Long.valueOf(body.get("questionId").toString()),
                (String) body.get("answer"),
                (Boolean) body.get("isCorrect"));
        return Result.ok();
    }

    @PostMapping("/exam/finish/{recordId}")
    public Result<ExamRecord> finishExam(@PathVariable Long recordId) {
        return Result.ok(practiceService.finishExam(recordId));
    }

    @GetMapping("/exam/record/{recordId}")
    public Result<List<Map<String, Object>>> recordDetail(@PathVariable Long recordId) {
        return Result.ok(practiceService.getRecordDetail(recordId));
    }

    @GetMapping("/records")
    public Result<List<ExamRecord>> myRecords(HttpServletRequest req) {
        User user = (User) req.getAttribute("currentUser");
        return Result.ok(practiceService.getMyRecords(user.getId()));
    }
}
