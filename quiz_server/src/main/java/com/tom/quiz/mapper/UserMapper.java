package com.tom.quiz.mapper;

import com.tom.quiz.models.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    @Select("SELECT * FROM users WHERE token = #{token}")
    User findByToken(String token);

    @Insert("INSERT INTO users (username, password, email, role, nickname, student_no, real_name, class_name, school_id, token) " +
            "VALUES (#{username}, #{password}, #{email}, #{role}, #{nickname}, #{studentNo}, #{realName}, #{className}, #{schoolId}, #{token})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET token = #{token} WHERE id = #{id}")
    int updateToken(@Param("id") Long id, @Param("token") String token);

    @Update("UPDATE users SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE users SET email = #{email} WHERE id = #{id}")
    int updateEmail(@Param("id") Long id, @Param("email") String email);

    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE users SET role = #{role} WHERE id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") String role);

    @Select("SELECT * FROM users WHERE role = #{role} ORDER BY created_at DESC")
    java.util.List<User> findByRole(String role);

    @Select("SELECT COUNT(*) FROM users")
    int count();
}
