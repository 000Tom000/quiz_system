package com.tom.quiz.mapper;

import com.tom.quiz.models.ExamRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExamRecordMapper {

    @Insert("INSERT INTO exam_records (user_id, question_set_id, mode, total_count, correct_count, wrong_count, duration, is_finished) " +
            "VALUES (#{userId}, #{questionSetId}, #{mode}, #{totalCount}, #{correctCount}, #{wrongCount}, #{duration}, #{isFinished})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExamRecord er);

    @Update("UPDATE exam_records SET total_count=#{totalCount}, correct_count=#{correctCount}, " +
            "wrong_count=#{wrongCount}, duration=#{duration}, is_finished=#{isFinished}, finished_at=#{finishedAt} WHERE id=#{id}")
    int update(ExamRecord er);

    @Select("SELECT * FROM exam_records WHERE id = #{id}")
    ExamRecord findById(Long id);

    @Select("SELECT * FROM exam_records WHERE user_id = #{userId} AND question_set_id = #{setId} ORDER BY created_at DESC")
    List<ExamRecord> findByUserAndSet(@Param("userId") Long userId, @Param("setId") Long setId);

    @Select("SELECT * FROM exam_records WHERE user_id = #{userId} AND question_set_id = #{setId} AND is_finished = 0 ORDER BY created_at DESC LIMIT 1")
    ExamRecord findUnfinished(@Param("userId") Long userId, @Param("setId") Long setId);

    @Select("SELECT er.*, qs.title AS set_title FROM exam_records er " +
            "LEFT JOIN question_sets qs ON er.question_set_id = qs.id " +
            "WHERE er.user_id = #{userId} ORDER BY er.created_at DESC")
    List<ExamRecord> findByUserId(Long userId);
}
