package com.tom.quiz.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String email;
    private String code;
    private String type;  // "password" / "code"
}
