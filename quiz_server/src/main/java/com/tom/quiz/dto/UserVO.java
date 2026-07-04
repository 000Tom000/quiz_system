package com.tom.quiz.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String nickname;
    private String studentNo;
    private String realName;
    private String className;
    private String token;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
