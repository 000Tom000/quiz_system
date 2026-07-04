package com.tom.quiz.mapper;

import com.tom.quiz.models.LoginAttempt;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginAttemptMapper {

    @Insert("INSERT INTO login_attempts (ip, username, success) VALUES (#{ip}, #{username}, #{success})")
    int insert(LoginAttempt la);

    @Select("SELECT COUNT(*) FROM login_attempts WHERE ip = #{ip} AND success = 0 " +
            "AND created_at > DATE_SUB(NOW(), INTERVAL 15 MINUTE)")
    int countRecentFailures(String ip);
}
