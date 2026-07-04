package com.tom.quiz.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamRecord {
    private Long id;
    private Long userId;
    private Long questionSetId;
    private String setTitle;     // JOIN question_sets 表填充，非 DB 字段
    private String mode;        // practice / exam
    private Integer totalCount;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer duration;   // 秒
    private Integer isFinished;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}
