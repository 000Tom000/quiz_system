package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuestionType {
    private Integer id;
    private String typeCode;
    private String typeName;
    private Integer hasOptions;
    private String optionType;
    private String answerFormat;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
