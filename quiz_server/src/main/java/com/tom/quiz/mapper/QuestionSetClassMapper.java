package com.tom.quiz.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QuestionSetClassMapper {

    @Insert("<script>" +
            "INSERT INTO question_set_classes (question_set_id, class_name) VALUES " +
            "<foreach collection='classNames' item='cn' separator=','>" +
            "(#{setId}, #{cn})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("setId") Long setId, @Param("classNames") List<String> classNames);

    @Select("SELECT class_name FROM question_set_classes WHERE question_set_id = #{setId}")
    List<String> findClassNamesBySetId(Long setId);

    @Delete("DELETE FROM question_set_classes WHERE question_set_id = #{setId}")
    int deleteBySetId(Long setId);
}
