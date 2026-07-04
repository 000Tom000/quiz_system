package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamAnswer {
    private Long id;
    private Long recordId;
    private Long questionId;
    private String userAnswer;  // JSON
    private Integer isCorrect;
    private LocalDateTime answeredAt;
}
