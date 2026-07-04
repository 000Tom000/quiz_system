package com.tom.quiz.mapper;

import com.tom.quiz.models.QuestionType;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QuestionTypeMapper {

    @Select("SELECT * FROM question_types ORDER BY sort_order")
    List<QuestionType> findAll();

    @Select("SELECT * FROM question_types WHERE id = #{id}")
    QuestionType findById(Integer id);

    @Select("SELECT * FROM question_types WHERE type_name = #{typeName}")
    QuestionType findByTypeName(String typeName);
}
