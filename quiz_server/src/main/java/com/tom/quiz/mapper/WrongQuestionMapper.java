package com.tom.quiz.mapper;

import com.tom.quiz.models.WrongQuestion;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WrongQuestionMapper {

    @Insert("INSERT INTO wrong_questions (user_id, question_id, question_set_id) " +
            "VALUES (#{userId}, #{questionId}, #{questionSetId})")
    int insert(WrongQuestion wq);

    @Delete("DELETE FROM wrong_questions WHERE id = #{id}")
    int delete(Long id);

    @Delete("DELETE FROM wrong_questions WHERE user_id = #{userId} AND question_id = #{questionId}")
    int deleteByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

    @Select("SELECT wq.* FROM wrong_questions wq WHERE user_id = #{userId} AND question_set_id = #{setId} ORDER BY created_at DESC")
    List<WrongQuestion> findByUserAndSet(@Param("userId") Long userId, @Param("setId") Long setId);

    @Select("SELECT wq.* FROM wrong_questions wq WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<WrongQuestion> findByUserId(Long userId);
}
