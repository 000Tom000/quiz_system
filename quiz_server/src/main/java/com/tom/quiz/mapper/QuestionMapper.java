package com.tom.quiz.mapper;

import com.tom.quiz.models.Question;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QuestionMapper {

    @Select("SELECT * FROM questions WHERE id = #{id}")
    Question findById(Long id);

    @Select("SELECT * FROM questions WHERE question_set_id = #{setId} AND status = 1 ORDER BY sort_order")
    List<Question> findBySetId(Long setId);

    @Select("SELECT * FROM questions WHERE question_set_id = #{setId} AND status = 1 AND difficulty = #{difficulty} ORDER BY sort_order")
    List<Question> findBySetIdAndDifficulty(@Param("setId") Long setId, @Param("difficulty") String difficulty);

    @Select("SELECT * FROM questions WHERE question_set_id = #{setId} AND status = 1 AND question_type_id = #{typeId} ORDER BY sort_order")
    List<Question> findBySetIdAndType(@Param("setId") Long setId, @Param("typeId") Integer typeId);

    @Select("SELECT COUNT(*) FROM questions WHERE question_set_id = #{setId} AND status = 1")
    int countBySetId(Long setId);

    @Insert("INSERT INTO questions (question_set_id, question_type_id, content, options, answer, explanation, difficulty, tags, sort_order) " +
            "VALUES (#{questionSetId}, #{questionTypeId}, #{content}, #{options}, #{answer}, #{explanation}, #{difficulty}, #{tags}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Question q);

    @Update("UPDATE questions SET content=#{content}, options=#{options}, answer=#{answer}, " +
            "explanation=#{explanation}, difficulty=#{difficulty}, tags=#{tags} WHERE id=#{id}")
    int update(Question q);

    @Delete("DELETE FROM questions WHERE id=#{id}")
    int delete(Long id);

    @Select("SELECT * FROM questions WHERE question_set_id = #{setId} AND content = #{content} AND status = 1 LIMIT 1")
    Question findByContent(@Param("setId") Long setId, @Param("content") String content);
}
