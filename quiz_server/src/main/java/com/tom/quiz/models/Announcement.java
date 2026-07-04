package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Announcement {
    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private Integer isActive;
    private LocalDateTime createdAt;
}
