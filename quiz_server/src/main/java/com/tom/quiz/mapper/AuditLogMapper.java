package com.tom.quiz.mapper;

import com.tom.quiz.models.AuditLog;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AuditLogMapper {

    @Insert("INSERT INTO audit_logs (user_id, username, action, target_type, target_id, detail, ip) " +
            "VALUES (#{userId}, #{username}, #{action}, #{targetType}, #{targetId}, #{detail}, #{ip})")
    int insert(AuditLog al);

    @Select("SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT #{limit}")
    List<AuditLog> findRecent(@Param("limit") int limit);
}
