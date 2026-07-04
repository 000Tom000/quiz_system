package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuestionSetClass {
    private Long id;
    private Long questionSetId;
    private String className;
    private LocalDateTime createdAt;
}
