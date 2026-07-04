package com.tom.quiz.service;

import com.tom.quiz.models.Question;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.util.List;

public interface QuestionService {

    Question addQuestion(Long teacherId, Question q);

    Question updateQuestion(Long teacherId, Question q);

    void deleteQuestion(Long teacherId, Long questionId);

    List<Question> getQuestionsBySetId(Long setId, String typeFilter, String difficulty);

    int importExcel(Long teacherId, Long setId, MultipartFile file);

    ByteArrayOutputStream exportExcel(Long setId);
}
