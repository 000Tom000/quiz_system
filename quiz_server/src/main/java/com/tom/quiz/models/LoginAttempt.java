package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoginAttempt {
    private Long id;
    private String ip;
    private String username;
    private Integer success;
    private LocalDateTime createdAt;
}
