package com.tom.quiz.mapper;

import com.tom.quiz.models.Favorite;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("INSERT INTO favorites (user_id, question_set_id) VALUES (#{userId}, #{questionSetId})")
    int insert(Favorite f);

    @Delete("DELETE FROM favorites WHERE user_id=#{userId} AND question_set_id=#{questionSetId}")
    int delete(@Param("userId") Long userId, @Param("questionSetId") Long questionSetId);

    @Select("SELECT * FROM favorites WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Favorite> findByUserId(Long userId);

    @Select("SELECT * FROM favorites WHERE user_id = #{userId} AND question_set_id = #{questionSetId}")
    Favorite findByUserAndSet(@Param("userId") Long userId, @Param("questionSetId") Long questionSetId);

    @Select("SELECT COUNT(*) FROM favorites WHERE question_set_id = #{questionSetId}")
    int countBySetId(Long questionSetId);
}
