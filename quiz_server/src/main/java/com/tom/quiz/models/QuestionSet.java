package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuestionSet {
    private Long id;
    private Long teacherId;
    private String teacherName;  // JOIN users 表填充，非 DB 字段
    private String title;
    private String description;
    private String remark;
    private String setType;    // practice / exam
    private Integer isPublic;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private Integer questionCount;
    private Integer favoriteCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
