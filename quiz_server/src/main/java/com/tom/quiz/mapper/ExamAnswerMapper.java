package com.tom.quiz.mapper;

import com.tom.quiz.models.ExamAnswer;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExamAnswerMapper {

    @Insert("INSERT INTO exam_answers (record_id, question_id, user_answer, is_correct, answered_at) " +
            "VALUES (#{recordId}, #{questionId}, #{userAnswer}, #{isCorrect}, #{answeredAt})")
    int insert(ExamAnswer ea);

    @Select("SELECT * FROM exam_answers WHERE record_id = #{recordId}")
    List<ExamAnswer> findByRecordId(Long recordId);

    @Select("SELECT ea.* FROM exam_answers ea " +
            "JOIN exam_records er ON ea.record_id = er.id " +
            "WHERE er.user_id = #{userId} AND ea.question_id = #{questionId} " +
            "ORDER BY ea.answered_at DESC")
    List<ExamAnswer> findHistoryByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);
}
