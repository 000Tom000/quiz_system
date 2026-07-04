package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WrongQuestion {
    private Long id;
    private Long userId;
    private Long questionId;
    private Long questionSetId;
    private LocalDateTime createdAt;
}
