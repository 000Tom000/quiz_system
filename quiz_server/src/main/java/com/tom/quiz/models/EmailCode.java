package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmailCode {
    private Long id;
    private String email;
    private String code;
    private String purpose;     // register / login / reset / modify
    private Integer isUsed;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
