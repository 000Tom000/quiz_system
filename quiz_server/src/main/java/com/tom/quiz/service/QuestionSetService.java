package com.tom.quiz.service;

import com.tom.quiz.models.QuestionSet;
import java.util.List;

public interface QuestionSetService {

    QuestionSet create(Long teacherId, QuestionSet qs, List<String> classNames);

    QuestionSet update(Long teacherId, QuestionSet qs, List<String> classNames);

    void delete(Long teacherId, Long setId);

    QuestionSet getById(Long setId);

    List<QuestionSet> getMySets(Long teacherId);

    List<QuestionSet> getVisibleSets(String className);

    List<QuestionSet> search(String className, String keyword);
}
