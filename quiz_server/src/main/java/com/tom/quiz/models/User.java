package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role;       // student / teacher / admin
    private String nickname;
    private String studentNo;
    private String realName;
    private String className;
    private Long schoolId;
    private String token;
    private String avatarUrl;
    private Integer status;    // 1=正常 0=禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
