package com.tom.quiz.mapper;

import com.tom.quiz.models.QuestionSet;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QuestionSetMapper {

    @Select("SELECT qs.*, u.nickname AS teacher_name FROM question_sets qs " +
            "LEFT JOIN users u ON qs.teacher_id = u.id WHERE qs.id = #{id}")
    QuestionSet findById(Long id);

    @Select("SELECT qs.*, u.nickname AS teacher_name FROM question_sets qs " +
            "LEFT JOIN users u ON qs.teacher_id = u.id WHERE qs.teacher_id = #{teacherId} ORDER BY qs.created_at DESC")
    List<QuestionSet> findByTeacherId(Long teacherId);

    @Select("SELECT qs.*, u.nickname AS teacher_name FROM question_sets qs " +
            "LEFT JOIN question_set_classes qsc ON qs.id = qsc.question_set_id " +
            "LEFT JOIN users u ON qs.teacher_id = u.id " +
            "WHERE qs.status = 1 AND (qs.is_public = 1 OR qsc.class_name = #{className}) " +
            "GROUP BY qs.id, u.nickname ORDER BY qs.created_at DESC")
    List<QuestionSet> findVisibleByClass(String className);

    @Select("SELECT qs.*, u.nickname AS teacher_name FROM question_sets qs " +
            "LEFT JOIN question_set_classes qsc ON qs.id = qsc.question_set_id " +
            "LEFT JOIN users u ON qs.teacher_id = u.id " +
            "WHERE qs.status = 1 AND (qs.is_public = 1 OR qsc.class_name = #{className}) " +
            "AND (qs.title LIKE CONCAT('%',#{keyword},'%') OR qs.description LIKE CONCAT('%',#{keyword},'%')) " +
            "GROUP BY qs.id, u.nickname ORDER BY qs.created_at DESC")
    List<QuestionSet> search(@Param("className") String className, @Param("keyword") String keyword);

    @Insert("INSERT INTO question_sets (teacher_id, title, description, remark, set_type, is_public, open_time, close_time) " +
            "VALUES (#{teacherId}, #{title}, #{description}, #{remark}, #{setType}, #{isPublic}, #{openTime}, #{closeTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QuestionSet qs);

    @Update("UPDATE question_sets SET title=#{title}, description=#{description}, remark=#{remark}, " +
            "is_public=#{isPublic}, open_time=#{openTime}, close_time=#{closeTime}, question_count=#{questionCount} " +
            "WHERE id=#{id} AND teacher_id=#{teacherId}")
    int update(QuestionSet qs);

    @Delete("DELETE FROM question_sets WHERE id=#{id} AND teacher_id=#{teacherId}")
    int delete(@Param("id") Long id, @Param("teacherId") Long teacherId);

    @Select("SELECT * FROM question_sets ORDER BY created_at DESC")
    List<QuestionSet> findAll();

    @Select("SELECT COUNT(*) FROM question_sets")
    int count();
}
