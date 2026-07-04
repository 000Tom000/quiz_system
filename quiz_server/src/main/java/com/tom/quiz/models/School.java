package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class School {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
