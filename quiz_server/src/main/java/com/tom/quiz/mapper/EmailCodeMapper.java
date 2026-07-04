package com.tom.quiz.mapper;

import com.tom.quiz.models.EmailCode;
import org.apache.ibatis.annotations.*;

@Mapper
public interface EmailCodeMapper {

    @Insert("INSERT INTO email_codes (email, code, purpose, expires_at) " +
            "VALUES (#{email}, #{code}, #{purpose}, #{expiresAt})")
    int insert(EmailCode ec);

    @Select("SELECT * FROM email_codes WHERE email = #{email} AND code = #{code} AND purpose = #{purpose} " +
            "AND is_used = 0 AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    EmailCode findByEmailCode(@Param("email") String email,
                              @Param("code") String code,
                              @Param("purpose") String purpose);

    @Update("UPDATE email_codes SET is_used = 1 WHERE id = #{id}")
    int markUsed(Long id);
}
