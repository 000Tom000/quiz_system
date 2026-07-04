package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Favorite {
    private Long id;
    private Long userId;
    private Long questionSetId;
    private LocalDateTime createdAt;
}
