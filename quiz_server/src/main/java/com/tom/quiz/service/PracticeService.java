package com.tom.quiz.service;

import com.tom.quiz.models.ExamRecord;
import com.tom.quiz.models.Question;
import java.util.List;
import java.util.Map;

public interface PracticeService {

    /** 开始刷题：获取题目列表（支持乱序、按题型筛选） */
    List<Question> startPractice(Long userId, Long setId, boolean shuffleQuestions, boolean shuffleOptions, String typeFilter);

    /** 普通模式：提交单题答案，立即返回对错 */
    Map<String, Object> submitAnswer(Long userId, Long questionId, String userAnswer);

    /** 添加到错题集 */
    void addToWrong(Long userId, Long questionId, Long questionSetId);

    /** 从错题集移除 */
    void removeFromWrong(Long userId, Long questionId);

    /** 获取错题集题目（setId=null 表示总错题集） */
    List<Map<String, Object>> getWrongQuestions(Long userId, Long setId, boolean shuffleQuestions, boolean shuffleOptions);

    /** 开始考试：创建答题记录 */
    ExamRecord startExam(Long userId, Long setId);

    /** 考试模式：保存单题答案 */
    void saveExamAnswer(Long recordId, Long questionId, String userAnswer, boolean isCorrect);

    /** 交卷 */
    ExamRecord finishExam(Long recordId);

    /** 获取未完成的考试记录 */
    ExamRecord getUnfinishedExam(Long userId, Long setId);

    /** 获取答题记录详情 */
    List<Map<String, Object>> getRecordDetail(Long recordId);

    /** 我的答题记录 */
    List<ExamRecord> getMyRecords(Long userId);

    /** 收藏试题集 */
    void favorite(Long userId, Long setId);

    /** 取消收藏 */
    void unfavorite(Long userId, Long setId);

    /** 是否已收藏 */
    boolean isFavorited(Long userId, Long setId);
}
