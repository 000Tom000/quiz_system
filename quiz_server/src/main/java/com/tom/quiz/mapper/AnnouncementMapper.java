package com.tom.quiz.mapper;

import com.tom.quiz.models.Announcement;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Select("SELECT * FROM announcements WHERE is_active = 1 ORDER BY created_at DESC")
    List<Announcement> findActive();

    @Select("SELECT * FROM announcements ORDER BY created_at DESC")
    List<Announcement> findAll();

    @Insert("INSERT INTO announcements (title, content, publisher_id) VALUES (#{title}, #{content}, #{publisherId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement a);

    @Update("UPDATE announcements SET is_active = #{isActive} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("isActive") Integer isActive);
}
