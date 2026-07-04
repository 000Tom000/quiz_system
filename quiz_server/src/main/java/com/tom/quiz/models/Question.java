package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Question {
    private Long id;
    private Long questionSetId;
    private Integer questionTypeId;
    private String content;       // 题干(HTML)
    private String options;       // JSON [{label:"A",content:"..."}]
    private String answer;        // JSON (格式由题型决定)
    private String explanation;   // 解析(HTML)
    private String difficulty;    // easy / medium / hard
    private String tags;          // 逗号分隔
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
