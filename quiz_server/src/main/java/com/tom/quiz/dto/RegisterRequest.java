package com.tom.quiz.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String code;
    private String studentNo;
    private String realName;
}
