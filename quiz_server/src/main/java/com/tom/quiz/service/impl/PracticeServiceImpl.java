package com.tom.quiz.service.impl;

import com.tom.quiz.mapper.*;
import com.tom.quiz.models.*;
import com.tom.quiz.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PracticeServiceImpl implements PracticeService {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionSetMapper questionSetMapper;
    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;
    @Autowired
    private ExamRecordMapper examRecordMapper;
    @Autowired
    private ExamAnswerMapper examAnswerMapper;
    @Autowired
    private FavoriteMapper favoriteMapper;

    // ===== 开始刷题 =====
    @Override
    public List<Question> startPractice(Long userId, Long setId, boolean shuffleQuestions, boolean shuffleOptions, String typeFilter) {
        List<Question> questions;
        if (typeFilter != null) {
            questions = questionMapper.findBySetIdAndType(setId, getTypeIdByName(typeFilter));
        } else {
            questions = questionMapper.findBySetId(setId);
        }

        if (shuffleQuestions) {
            Collections.shuffle(questions);
        }
        if (shuffleOptions) {
            for (Question q : questions) {
                q.setOptions(shuffleOptionsJson(q.getOptions()));
            }
        }
        return questions;
    }

    // ===== 提交单题答案 =====
    @Override
    public Map<String, Object> submitAnswer(Long userId, Long questionId, String userAnswer) {
        Question q = questionMapper.findById(questionId);
        if (q == null) throw new RuntimeException("题目不存在");

        boolean correct = checkAnswer(q.getAnswer(), userAnswer, q.getQuestionTypeId());
        Map<String, Object> result = new HashMap<>();
        result.put("correct", correct);
        result.put("answer", q.getAnswer());
        result.put("explanation", q.getExplanation());
        return result;
    }

    // ===== 错题管理 =====
    @Override
    @Transactional
    public void addToWrong(Long userId, Long questionId, Long questionSetId) {
        WrongQuestion wq = new WrongQuestion();
        wq.setUserId(userId);
        wq.setQuestionId(questionId);
        wq.setQuestionSetId(questionSetId);
        try {
            wrongQuestionMapper.insert(wq);
        } catch (Exception ignored) {
            // 已存在则忽略
        }
    }

    @Override
    public void removeFromWrong(Long userId, Long questionId) {
        wrongQuestionMapper.deleteByUserAndQuestion(userId, questionId);
    }

    @Override
    public List<Map<String, Object>> getWrongQuestions(Long userId, Long setId, boolean shuffleQuestions, boolean shuffleOptions) {
        List<WrongQuestion> wqs = (setId != null)
                ? wrongQuestionMapper.findByUserAndSet(userId, setId)
                : wrongQuestionMapper.findByUserId(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WrongQuestion wq : wqs) {
            Question q = questionMapper.findById(wq.getQuestionId());
            if (q == null) continue;
            QuestionSet qs = questionSetMapper.findById(wq.getQuestionSetId());
            Map<String, Object> item = new HashMap<>();
            item.put("wrongId", wq.getId());
            item.put("question", q);
            item.put("setName", qs != null ? qs.getTitle() : "");
            result.add(item);
        }

        if (shuffleQuestions) Collections.shuffle(result);
        if (shuffleOptions) {
            for (Map<String, Object> item : result) {
                Question q = (Question) item.get("question");
                q.setOptions(shuffleOptionsJson(q.getOptions()));
            }
        }
        return result;
    }

    // ===== 考试模式 =====
    @Override
    @Transactional
    public ExamRecord startExam(Long userId, Long setId) {
        // 检查是否有未完成记录
        ExamRecord unfinished = examRecordMapper.findUnfinished(userId, setId);
        if (unfinished != null) return unfinished;

        ExamRecord er = new ExamRecord();
        er.setUserId(userId);
        er.setQuestionSetId(setId);
        er.setMode("exam");
        er.setTotalCount(questionMapper.countBySetId(setId));
        er.setCorrectCount(0);
        er.setWrongCount(0);
        er.setDuration(0);
        er.setIsFinished(0);
        examRecordMapper.insert(er);
        return er;
    }

    @Override
    public void saveExamAnswer(Long recordId, Long questionId, String userAnswer, boolean isCorrect) {
        ExamAnswer ea = new ExamAnswer();
        ea.setRecordId(recordId);
        ea.setQuestionId(questionId);
        ea.setUserAnswer(userAnswer);
        ea.setIsCorrect(isCorrect ? 1 : 0);
        ea.setAnsweredAt(LocalDateTime.now());
        examAnswerMapper.insert(ea);

        // 更新记录中的计数
        ExamRecord er = examRecordMapper.findById(recordId);
        if (isCorrect) er.setCorrectCount(er.getCorrectCount() + 1);
        else er.setWrongCount(er.getWrongCount() + 1);
        examRecordMapper.update(er);
    }

    @Override
    @Transactional
    public ExamRecord finishExam(Long recordId) {
        ExamRecord er = examRecordMapper.findById(recordId);
        if (er == null) throw new RuntimeException("记录不存在");
        er.setIsFinished(1);
        er.setFinishedAt(LocalDateTime.now());
        examRecordMapper.update(er);
        return er;
    }

    @Override
    public ExamRecord getUnfinishedExam(Long userId, Long setId) {
        return examRecordMapper.findUnfinished(userId, setId);
    }

    @Override
    public List<Map<String, Object>> getRecordDetail(Long recordId) {
        List<ExamAnswer> answers = examAnswerMapper.findByRecordId(recordId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamAnswer ea : answers) {
            Question q = questionMapper.findById(ea.getQuestionId());
            Map<String, Object> item = new HashMap<>();
            item.put("answer", ea);
            item.put("question", q);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<ExamRecord> getMyRecords(Long userId) {
        return examRecordMapper.findByUserId(userId);
    }

    // ===== 收藏 =====
    @Override
    @Transactional
    public void favorite(Long userId, Long setId) {
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setQuestionSetId(setId);
        favoriteMapper.insert(f);

        // 更新收藏数
        QuestionSet qs = questionSetMapper.findById(setId);
        qs.setFavoriteCount(favoriteMapper.countBySetId(setId));
        questionSetMapper.update(qs);
    }

    @Override
    @Transactional
    public void unfavorite(Long userId, Long setId) {
        favoriteMapper.delete(userId, setId);

        QuestionSet qs = questionSetMapper.findById(setId);
        qs.setFavoriteCount(favoriteMapper.countBySetId(setId));
        questionSetMapper.update(qs);
    }

    @Override
    public boolean isFavorited(Long userId, Long setId) {
        return favoriteMapper.findByUserAndSet(userId, setId) != null;
    }

    // ===== 辅助 =====
    private boolean checkAnswer(String correctAnswer, String userAnswer, Integer typeId) {
        if (correctAnswer == null || userAnswer == null) return false;
        // 简化比对：去除空格引号后比较
        String c = correctAnswer.trim().replaceAll("[\"\\[\\]]", "");
        String u = userAnswer.trim().replaceAll("[\"\\[\\]]", "");
        return c.equalsIgnoreCase(u);
    }

    private String shuffleOptionsJson(String optionsJson) {
        if (optionsJson == null) return null;
        // 简单打乱：解析 JSON 数组，shuffle，重新拼装
        try {
            String inner = optionsJson.trim().replaceAll("^\\[|\\]$", "");
            if (inner.isEmpty()) return optionsJson;
            String[] parts = inner.split("\\},\\{");
            List<String> list = new ArrayList<>(Arrays.asList(parts));
            Collections.shuffle(list);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(list.get(i));
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            return optionsJson;
        }
    }

    private Integer getTypeIdByName(String typeName) {
        // 直接通过 mapper 查...但我们没注入 QuestionTypeMapper
        // 简单映射常见题型
        return switch (typeName) {
            case "单选题" -> 1;
            case "多选题" -> 2;
            case "判断题" -> 3;
            case "填空题" -> 4;
            case "简答题" -> 5;
            case "编程题" -> 6;
            default -> null;
        };
    }
}
