package com.tom.quiz.mapper;

import com.tom.quiz.models.PresetStudent;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PresetStudentMapper {

    @Select("SELECT * FROM preset_students WHERE student_no = #{studentNo} AND name = #{name} AND is_registered = 0")
    PresetStudent findByStudentNoAndName(@Param("studentNo") String studentNo, @Param("name") String name);

    @Update("UPDATE preset_students SET is_registered = 1 WHERE id = #{id}")
    int markRegistered(Long id);
}
