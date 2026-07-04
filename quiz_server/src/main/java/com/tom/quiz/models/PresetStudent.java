package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PresetStudent {
    private Long id;
    private String studentNo;
    private String name;
    private String className;
    private Long schoolId;
    private Integer isRegistered;
    private LocalDateTime createdAt;
}
